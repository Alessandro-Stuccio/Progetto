package com.project.tesi.facade.impl;

import com.project.tesi.dto.request.ModeratorUserUpdateRequest;
import com.project.tesi.dto.request.UserCreateRequestDTO;
import com.project.tesi.dto.response.SubscriptionResponse;
import com.project.tesi.dto.response.UserResponse;
import com.project.tesi.enums.PaymentFrequency;
import com.project.tesi.enums.Role;
import com.project.tesi.exception.common.ResourceAlreadyExistsException;
import com.project.tesi.exception.common.UnauthorizedAccessException;
import com.project.tesi.facade.ModeratorFacade;
import com.project.tesi.facade.SubscriptionFacade;
import com.project.tesi.mapper.SubscriptionMapper;
import com.project.tesi.mapper.UserMapper;
import com.project.tesi.model.Plan;
import com.project.tesi.model.User;
import com.project.tesi.service.PlanService;
import com.project.tesi.service.SubscriptionService;
import com.project.tesi.service.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementazione di {@link com.project.tesi.facade.ModeratorFacade}.
 * Gestisce utenti, abbonamenti e chat per il ruolo {@code MODERATOR}.
 * Funge anche da classe base per {@link AdminFacadeImpl}.
 */
@Primary
@Component
public class ModeratorFacadeImpl implements ModeratorFacade {



    protected final SubscriptionMapper subscriptionMapper;
    protected final UserMapper userMapper;
    protected final UserService userService;
    protected final SubscriptionService subscriptionService;
    protected final PlanService planService;
    protected final SubscriptionFacade subscriptionFacade;

    public ModeratorFacadeImpl(UserService userService,
                               SubscriptionService subscriptionService,
                               UserMapper userMapper,
                               SubscriptionMapper subscriptionMapper,
                               PlanService planService,
                               SubscriptionFacade subscriptionFacade) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.planService = planService;
        this.subscriptionFacade = subscriptionFacade;
    }

    /**
     * Restituisce gli utenti gestibili dal chiamante in base al suo ruolo.
     * Un {@code ADMIN} vede tutti gli utenti; un {@code MODERATOR} vede solo
     * i ruoli definiti da {@link com.project.tesi.enums.Role#getManagebleRoles}.
     *
     * @param user utente autenticato che effettua la richiesta
     * @return lista di DTO degli utenti visibili
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getManageableUsers(User user) {
        List<User> l=userService.findAll();
        if(user.getRole()== Role.ADMIN)return userMapper.toAdminResponse(l);
        else return l.stream()
                .filter(u -> Role.getManagebleRoles(user.getRole()).contains(u.getRole()))
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getChatContacts() {
        return userService.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.INSURANCE_MANAGER || u.getRole() == Role.MODERATOR)
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionService.getAllSubscriptions().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    /**
     * Crea un nuovo utente verificando che il ruolo target sia tra quelli
     * gestibili dal chiamante. Codifica la password, assegna eventuali
     * professionisti al client e, se forniti piano e frequenza di pagamento,
     * attiva l'abbonamento tramite {@link com.project.tesi.facade.SubscriptionFacade}.
     *
     * @param request dati del nuovo utente
     * @param user    utente moderatore/admin che esegue la creazione
     * @return DTO dell'utente creato
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il ruolo target non è gestibile
     * @throws com.project.tesi.exception.common.ResourceAlreadyExistsException se l'email è già in uso
     */
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequestDTO request,User user) {
        Role targetRole = Role.valueOf(request.role());
        if (!Role.getManagebleRoles(user.getRole()).contains(targetRole)) {
            throw new UnauthorizedAccessException(
                    "Il moderatore non può creare utenti con ruolo " + targetRole + ".");
        }
        return buildAndSaveUser(request, targetRole);
    }

    /**
     * Aggiorna i dati anagrafici di un utente esistente.
     * Verifica che il ruolo del target sia gestibile dal chiamante e
     * controlla l'unicità della nuova email (se modificata).
     *
     * @param id      ID dell'utente da aggiornare
     * @param request nuovi dati da applicare
     * @param user    utente moderatore/admin che esegue la modifica
     * @return DTO dell'utente aggiornato
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il ruolo target non è gestibile
     * @throws com.project.tesi.exception.common.ResourceAlreadyExistsException se la nuova email è già in uso
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, ModeratorUserUpdateRequest request, User user) {

        User target = userService.getUserById(id);
        if (!Role.getManagebleRoles(user.getRole()).contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "Il moderatore non può modificare utenti con ruolo " + target.getRole() + ".");
        }

        String email = request.email();
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(target.getEmail())) {
            if (userService.existsUserByEmailExcluding(email, id)) {
                throw new ResourceAlreadyExistsException("Utente", "email", email);
            }
            target.setEmail(email);
        }

        applyUserUpdates(target, request);

        return userMapper.toAdminResponse(userService.save(target));
    }

    /**
     * Esegue il soft delete di un utente impostando il flag {@code deleted=true}.
     * Verifica che il ruolo del target sia gestibile dal chiamante prima di delegare
     * a {@link com.project.tesi.service.UserService#deleteUser}.
     *
     * @param id   ID dell'utente da eliminare
     * @param user utente moderatore/admin che richiede l'eliminazione
     * @throws com.project.tesi.exception.common.UnauthorizedAccessException se il ruolo target non è gestibile
     */
    @Override
    @Transactional
    public void deleteUser(Long id, User user) {

        User target = userService.getUserById(id);
        if (!Role.getManagebleRoles(user.getRole()).contains(target.getRole())) {
            throw new UnauthorizedAccessException(
                    "L'utente " + user.getRole() + " non può eliminare utenti con ruolo " + target.getRole() + ".");
        }

        userService.deleteUser(id);
    }

    @Override
    @Transactional
    public SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri) {
        if (pt < 0 || nutri < 0) {
            throw new IllegalArgumentException("I crediti non possono essere negativi.");
        }
        return subscriptionMapper.toResponse(subscriptionService.updateSubscriptionCredits(id, pt, nutri));
    }

    protected UserResponse buildAndSaveUser(UserCreateRequestDTO request, Role targetRole) {
        String email = request.email();
        String firstName = request.firstName();
        String lastName = request.lastName();
        String password = request.password();
        if (email == null || firstName == null || lastName == null || password == null) {
            throw new IllegalArgumentException(
                    "Campi obbligatori mancanti (email, firstName, lastName, password, role).");
        }

        if (userService.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Utente", "email", email);
        }

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(userService.encodePassword(password))
                .role(targetRole)
                .build();

        if (targetRole == Role.CLIENT) {
            if (request.assignedPTId() != null) {
                User assignedPT = userService.getUserById(request.assignedPTId());
                if (assignedPT.getRole() != Role.PERSONAL_TRAINER) {
                    throw new UnauthorizedAccessException("L'utente assegnato come PT non è un PERSONAL_TRAINER");
                }
                user.setAssignedPT(assignedPT);
            }
            if (request.assignedNutritionistId() != null) {
                User assignedNutri = userService.getUserById(request.assignedNutritionistId());
                if (assignedNutri.getRole() != Role.NUTRITIONIST) {
                    throw new UnauthorizedAccessException("L'utente assegnato come nutrizionista non è un NUTRITIONIST");
                }
                user.setAssignedNutritionist(assignedNutri);
            }
        }

        User savedUser = userService.save(user);

        if (targetRole == Role.CLIENT && request.planId() != null && request.paymentFrequency() != null) {
            PaymentFrequency freq;
            try {
                freq = PaymentFrequency.valueOf(request.paymentFrequency());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Frequenza di pagamento non valida: " + request.paymentFrequency());
            }
            Plan plan = planService.getPlanById(request.planId());
            subscriptionFacade.activateSubscription(savedUser, plan, freq);
        }

        return userMapper.toAdminResponse(savedUser);
    }

    protected void applyUserUpdates(User target, ModeratorUserUpdateRequest request) {
        String firstName = request.firstName();
        if (firstName != null && !firstName.isBlank()) target.setFirstName(firstName);

        String lastName = request.lastName();
        if (lastName != null && !lastName.isBlank()) target.setLastName(lastName);

        String password = request.password();
        if (password != null && !password.isBlank()) target.setPassword(userService.encodePassword(password));

    }
}
