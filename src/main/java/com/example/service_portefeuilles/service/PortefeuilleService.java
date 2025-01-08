package com.example.service_portefeuilles.service;
import com.example.service_portefeuilles.client.UserClient;
import com.example.service_portefeuilles.dto.PortefeuilleDto;
import com.example.service_portefeuilles.client.ExpenseClient;
import com.example.service_portefeuilles.dto.*;
import com.example.service_portefeuilles.maper.PortefeuilleMapper;
import com.example.service_portefeuilles.model.Alert;
import com.example.service_portefeuilles.model.Portefeuille;
import com.example.service_portefeuilles.repository.PortefeuilleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PortefeuilleService {

    @Autowired
    private PortefeuilleRepository portefeuilleRepository;

//    @Autowired
//    private CompteBancaireRepository compteBancaireRepository;
    @Autowired
    private PortefeuilleMapper mapper;

    @Autowired
    private UserClient userClient;
//    @Autowired
//    private AlimentationRepository alimentationRepository;
    @Autowired
    private ExpenseClient expenseClient;



    public List<PortefeuilleDto> recupererPortefeuillesEtDépensesParUtilisateur(Long utilisateurId) {
        return portefeuilleRepository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(portefeuille -> {
                    // Récupérer les dépenses via Feign
                    List<ExpenseDTO> expenses = expenseClient.getExpensesByPortefeuille(portefeuille.getId());

                    // Créer le DTO en incluant les dépenses
                    return new PortefeuilleDto(
                            portefeuille.getId(),
                            utilisateurId,
                            portefeuille.getBalance(),
                            portefeuille.getDevise(),
                            expenses
                    );
                })
                .collect(Collectors.toList());
    }

    public SoldeResponseDto consulterSolde(Long portefeuilleId) {
        Portefeuille portefeuille = portefeuilleRepository.findById(portefeuilleId)
                .orElseThrow(() -> new RuntimeException("Portefeuille non trouvé !"));
        return new SoldeResponseDto(portefeuille.getBalance(), portefeuille.getDevise());
    }


    public Alert creerPortefeuille(CreationPortefeuilleRequestDto request) {
        Optional<Portefeuille> existingPortefeuille = portefeuilleRepository.findByUtilisateurIdAndDevise(request.getutilisateurId(), request.getDevise());

        if (existingPortefeuille.isPresent()) {
            return new Alert("Un portefeuille existe déjà pour cet utilisateur et cette devise.",LocalDate.now(), false);
        }

        Portefeuille portefeuille = new Portefeuille();
        portefeuille.setUtilisateurId(request.getutilisateurId());
        portefeuille.setDevise(request.getDevise());
        portefeuille.setBalance(request.getBalanceInitiale());
        portefeuilleRepository.save(portefeuille);

        return new Alert("Portefeuille créé avec succès avec la devise " + request.getDevise(),LocalDate.now(), true);
    }


    public PortefeuilleDto getById(Long id){
        return mapper.toDTO(portefeuilleRepository.findById(id).get());
    }

    @Transactional
    public Portefeuille debitPortefeuille(Long portefeuilleId, Double amount) {
        Portefeuille portefeuille = portefeuilleRepository.findById(portefeuilleId)
                .orElseThrow(() -> new RuntimeException("Portefeuille introuvable"));

        if (portefeuille.getBalance() < amount) {
            throw new RuntimeException("Solde insuffisant pour effectuer cette opération");
        }

        portefeuille.setBalance(portefeuille.getBalance() - amount);
        return portefeuilleRepository.save(portefeuille);
    }


    @Transactional
    public Portefeuille creditPortefeuille(Long portefeuilleId, Double amount) {
        Portefeuille portefeuille = portefeuilleRepository.findById(portefeuilleId)
                .orElseThrow(() -> new RuntimeException("Portefeuille introuvable"));

        portefeuille.setBalance(portefeuille.getBalance() + amount);
        return portefeuilleRepository.save(portefeuille);
    }


    @Transactional
    public Alert alimenterDepenseExistante(Long portefeuilleId, Long depenseId, Double montantSupplementaire) {
        // Récupérer le portefeuille
        Portefeuille portefeuille = portefeuilleRepository.findById(portefeuilleId)
                .orElseThrow(() -> new RuntimeException("Portefeuille introuvable"));

        // Vérifier que le solde du portefeuille est suffisant
        if (portefeuille.getBalance() < montantSupplementaire) {
            return new Alert("Solde insuffisant", LocalDate.now(), false);
        }

        // Alimenter la dépense via Feign
        boolean updateSuccess = expenseClient.alimenterDepense(depenseId, montantSupplementaire);

        if (!updateSuccess) {
            throw new RuntimeException("Erreur lors de l'alimentation de la dépense");
        }

        // Mettre à jour le solde du portefeuille
        portefeuille.setBalance(portefeuille.getBalance() - montantSupplementaire);
        portefeuilleRepository.save(portefeuille);

        return new Alert("Dépense alimentée avec succès", LocalDate.now(), true);
    }
}
