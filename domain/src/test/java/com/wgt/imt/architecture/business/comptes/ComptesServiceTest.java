package com.wgt.imt.architecture.business.comptes;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.exception.NotFoundException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import com.wgt.imt.architecture.business.comptes.model.Mouvement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe ComptesService.
 * Vérifie le bon fonctionnement des opérations CRUD sur les comptes bancaires.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComptesService - Tests unitaires")
class ComptesServiceTest {

    @Mock
    private ClientStorageProvider storageProvider;

    private ComptesService comptesService;

    private UUID clientIdentifier;
    private UUID compteIdentifier;
    private Client client;
    private Compte compte1;
    private Compte compte2;

    @BeforeEach
    void setUp() {
        comptesService = new ComptesService(storageProvider);

        clientIdentifier = UUID.randomUUID();
        compteIdentifier = UUID.randomUUID();

        compte1 = Compte.builder()
                .clientIdentifier(clientIdentifier)
                .name("Compte_Courant")
                .type(TypeCompteEnum.COMPTE_COURANT)
                .solde(1000.0)
                .build();

        compte2 = Compte.builder()
                .clientIdentifier(clientIdentifier)
                .name("Livret_A")
                .type(TypeCompteEnum.LIVRET_A)
                .solde(5000.0)
                .build();

        Set<Compte> comptes = new HashSet<>(Arrays.asList(compte1, compte2));

        client = Client.builder()
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .comptes(comptes)
                .build();
    }

    @Nested
    @DisplayName("getAllFilteredByClient() - Récupération de tous les comptes d'un client")
    class GetAllFilteredByClientTests {

        @Test
        @DisplayName("Doit retourner tous les comptes du client")
        void shouldReturnAllAccountsForClient() {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            Collection<Compte> result = comptesService.getAllFilteredByClient(clientIdentifier);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .contains(compte1, compte2);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit retourner une collection vide si le client n'existe pas")
        void shouldReturnEmptyCollectionWhenClientDoesNotExist() {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            // When
            Collection<Compte> result = comptesService.getAllFilteredByClient(clientIdentifier);

            // Then
            assertThat(result)
                    .isNotNull()
                    .isEmpty();
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit retourner une collection vide si le client n'a pas de comptes")
        void shouldReturnEmptyCollectionWhenClientHasNoAccounts() {
            // Given
            Client clientSansComptes = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientSansComptes));

            // When
            Collection<Compte> result = comptesService.getAllFilteredByClient(clientIdentifier);

            // Then
            assertThat(result)
                    .isNotNull()
                    .isEmpty();
            verify(storageProvider, times(1)).get(clientIdentifier);
        }
    }

    @Nested
    @DisplayName("getOneFilteredByClient() - Récupération d'un compte spécifique")
    class GetOneFilteredByClientTests {

        @Test
        @DisplayName("Doit retourner le compte correspondant à l'identifiant")
        void shouldReturnAccountMatchingIdentifier() {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            Optional<Compte> result = comptesService.getOneFilteredByClient(clientIdentifier, compte1.getIdentifier());

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(compte1);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit retourner Optional.empty() si le compte n'existe pas")
        void shouldReturnEmptyOptionalWhenAccountDoesNotExist() {
            // Given
            UUID inexistantCompteId = UUID.randomUUID();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            Optional<Compte> result = comptesService.getOneFilteredByClient(clientIdentifier, inexistantCompteId);

            // Then
            assertThat(result).isEmpty();
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit retourner Optional.empty() si le client n'existe pas")
        void shouldReturnEmptyOptionalWhenClientDoesNotExist() {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            // When
            Optional<Compte> result = comptesService.getOneFilteredByClient(clientIdentifier, compte1.getIdentifier());

            // Then
            assertThat(result).isEmpty();
            verify(storageProvider, times(1)).get(clientIdentifier);
        }
    }

    @Nested
    @DisplayName("create() - Création d'un nouveau compte")
    class CreateTests {

        @Test
        @DisplayName("Doit créer et retourner le nouveau compte")
        void shouldCreateAndReturnNewAccount() throws ImtException {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("LDDS")
                    .type(TypeCompteEnum.LDDS)
                    .solde(0.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            Compte result = comptesService.create(clientIdentifier, newCompte);

            // Then
            assertThat(result).isEqualTo(newCompte);

            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider, times(1)).save(clientCaptor.capture());

            Client savedClient = clientCaptor.getValue();
            assertThat(savedClient.getComptes()).contains(newCompte);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit lever une NotFoundException si le client n'existe pas")
        void shouldThrowNotFoundExceptionWhenClientDoesNotExist() {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("LDDS")
                    .type(TypeCompteEnum.LDDS)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> comptesService.create(clientIdentifier, newCompte))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Le client")
                    .hasMessageContaining(clientIdentifier.toString())
                    .hasMessageContaining("n'existe pas");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit ajouter le compte aux comptes existants du client")
        void shouldAddAccountToExistingAccounts() throws ImtException {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("LDDS")
                    .type(TypeCompteEnum.LDDS)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesService.create(clientIdentifier, newCompte);

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider, times(1)).save(clientCaptor.capture());

            Client savedClient = clientCaptor.getValue();
            assertThat(savedClient.getComptes())
                    .hasSize(3)
                    .contains(compte1, compte2, newCompte);
        }
    }

    @Nested
    @DisplayName("update() - Mise à jour d'un compte existant")
    class UpdateTests {

        @Test
        @DisplayName("Doit mettre à jour le compte et publier un événement")
        void shouldUpdateAccountAndPublishEvent() throws ImtException {
            // Given
            Compte updatedCompte = compte1.toBuilder()
                    .solde(2000.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesService.update(clientIdentifier, updatedCompte);

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider, times(1)).save(clientCaptor.capture());

            Client savedClient = clientCaptor.getValue();
            assertThat(savedClient.getComptes()).contains(updatedCompte);

        }

        @Test
        @DisplayName("Doit lever une NotFoundException si le client n'existe pas")
        void shouldThrowNotFoundExceptionWhenClientDoesNotExist() {
            // Given
            Compte updatedCompte = compte1.toBuilder().solde(2000.0).build();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> comptesService.update(clientIdentifier, updatedCompte))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Le client")
                    .hasMessageContaining("n'existe pas");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit remplacer l'ancien compte par le nouveau")
        void shouldReplaceOldAccountWithNew() throws ImtException {
            // Given
            Compte updatedCompte = compte1.toBuilder()
                    .name("Nouveau_Nom")
                    .solde(3000.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesService.update(clientIdentifier, updatedCompte);

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider, times(1)).save(clientCaptor.capture());

            Client savedClient = clientCaptor.getValue();
            assertThat(savedClient.getComptes())
                    .hasSize(2)
                    .contains(updatedCompte, compte2)
                    .doesNotContain(compte1);
        }

        @Test
        @DisplayName("Doit gérer les comptes null du client")
        void shouldHandleNullAccountsForClient() throws ImtException {
            // Given
            Client clientAvecComptesNull = Client.builder()
                    .lastname("Test")
                    .firstname("Test")
                    .genre(GenreEnum.HOMME)
                    .comptes(null)
                    .build();

            Compte updatedCompte = compte1.toBuilder().solde(2000.0).build();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientAvecComptesNull));
            when(storageProvider.save(any(Client.class))).thenReturn(clientAvecComptesNull);

            // When
            comptesService.update(clientIdentifier, updatedCompte);

            // Then
            verify(storageProvider, times(1)).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("applyAgiosIfNeeded() - Application des agios")
    class ApplyAgiosTests {

        @Test
        @DisplayName("Applique les agios et signale le changement lorsque le solde est négatif")
        void shouldApplyAgiosWhenBalanceIsNegative() throws ImtException {
            Compte compteNegatif = compte1.toBuilder().solde(-100.0).build();
            Client clientAvecCompteNegatif = client.toBuilder()
                    .comptes(new HashSet<>(Collections.singletonList(compteNegatif)))
                    .build();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientAvecCompteNegatif));
            when(storageProvider.save(any(Client.class))).thenReturn(clientAvecCompteNegatif);

            assertThat(comptesService.applyAgiosIfNeeded(clientIdentifier, compteNegatif.getIdentifier())).isTrue();

            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider).save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getComptes())
                    .filteredOn(c -> c.getIdentifier().equals(compteNegatif.getIdentifier()))
                    .singleElement()
                    .extracting(Compte::getSolde)
                    .isEqualTo(-105.0);
        }

        @Test
        @DisplayName("Ne sauvegarde rien si le solde est positif ou nul")
        void shouldNotApplyAgiosWhenBalanceIsNotNegative() throws ImtException {
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            assertThat(comptesService.applyAgiosIfNeeded(clientIdentifier, compte1.getIdentifier())).isFalse();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Retourne false si le compte est absent")
        void shouldReturnFalseWhenAccountDoesNotExist() throws ImtException {
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            assertThat(comptesService.applyAgiosIfNeeded(clientIdentifier, UUID.randomUUID())).isFalse();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit lever une NotFoundException si le client n'existe pas")
        void shouldThrowNotFoundExceptionWhenClientDoesNotExist() {
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> comptesService.applyAgiosIfNeeded(clientIdentifier, compteIdentifier))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Le client")
                    .hasMessageContaining("n'existe pas");
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Ne modifie pas les autres comptes")
        void shouldNotModifyOtherAccounts() throws ImtException {
            Compte compteNegatif = compte1.toBuilder().solde(-100.0).build();
            Client clientAvecComptes = client.toBuilder()
                    .comptes(new HashSet<>(Arrays.asList(compteNegatif, compte2)))
                    .build();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientAvecComptes));
            when(storageProvider.save(any(Client.class))).thenReturn(clientAvecComptes);

            comptesService.applyAgiosIfNeeded(clientIdentifier, compteNegatif.getIdentifier());

            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider).save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getComptes())
                    .filteredOn(c -> c.getIdentifier().equals(compte2.getIdentifier()))
                    .singleElement()
                    .extracting(Compte::getSolde)
                    .isEqualTo(5000.0);
        }

        @Test
        @DisplayName("Gère une collection de comptes nulle sans sauvegarder")
        void shouldHandleNullAccounts() throws ImtException {
            Client clientSansComptes = Client.builder()
                    .lastname("Test")
                    .firstname("Test")
                    .genre(GenreEnum.HOMME)
                    .comptes(null)
                    .build();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientSansComptes));

            assertThat(comptesService.applyAgiosIfNeeded(clientIdentifier, compteIdentifier)).isFalse();
            verify(storageProvider, never()).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("addIntoHistory() - Historique des mouvements")
    class AddIntoHistoryTests {

        @Test
        @DisplayName("Ajoute le message à l'historique du compte")
        void shouldAddMovementToAccountHistory() throws ImtException {
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            comptesService.addIntoHistory(clientIdentifier, compte1.getIdentifier(), "Dépôt");

            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider).save(clientCaptor.capture());
            Compte updatedCompte = clientCaptor.getValue().getComptes().stream()
                    .filter(compte -> compte.getIdentifier().equals(compte1.getIdentifier()))
                    .findFirst()
                    .orElseThrow();
            assertThat(updatedCompte.getHistorique())
                    .singleElement()
                    .extracting(Mouvement::getMessage)
                    .isEqualTo("Dépôt");
        }

        @Test
        @DisplayName("N'effectue aucune sauvegarde si le compte n'existe pas")
        void shouldNotSaveWhenAccountDoesNotExist() throws ImtException {
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            comptesService.addIntoHistory(clientIdentifier, UUID.randomUUID(), "Dépôt");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Lève une NotFoundException si le client n'existe pas")
        void shouldThrowWhenClientDoesNotExist() {
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> comptesService.addIntoHistory(clientIdentifier, compteIdentifier, "Dépôt"))
                    .isInstanceOf(NotFoundException.class);
            verify(storageProvider, never()).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("delete() - Suppression d'un compte")
    class DeleteTests {

        @Test
        @DisplayName("Doit supprimer le compte du client")
        void shouldDeleteAccountFromClient() throws ImtException {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesService.delete(clientIdentifier, compte1.getIdentifier());

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider, times(1)).save(clientCaptor.capture());

            Client savedClient = clientCaptor.getValue();
            assertThat(savedClient.getComptes())
                    .hasSize(1)
                    .contains(compte2)
                    .doesNotContain(compte1);
        }

        @Test
        @DisplayName("Doit lever une NotFoundException si le client n'existe pas")
        void shouldThrowNotFoundExceptionWhenClientDoesNotExist() {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> comptesService.delete(clientIdentifier, compte1.getIdentifier()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Le client")
                    .hasMessageContaining("n'existe pas");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Ne doit pas modifier les autres comptes si le compte n'existe pas")
        void shouldNotModifyOtherAccountsIfAccountDoesNotExist() throws ImtException {
            // Given
            UUID inexistantCompteId = UUID.randomUUID();
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesService.delete(clientIdentifier, inexistantCompteId);

            // Then
            ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
            verify(storageProvider, times(1)).save(clientCaptor.capture());

            Client savedClient = clientCaptor.getValue();
            assertThat(savedClient.getComptes())
                    .hasSize(2)
                    .contains(compte1, compte2);
        }

        @Test
        @DisplayName("Doit gérer les comptes null du client")
        void shouldHandleNullAccountsForClient() throws ImtException {
            // Given
            Client clientAvecComptesNull = Client.builder()
                    .lastname("Test")
                    .firstname("Test")
                    .genre(GenreEnum.HOMME)
                    .comptes(null)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientAvecComptesNull));
            when(storageProvider.save(any(Client.class))).thenReturn(clientAvecComptesNull);

            // When & Then - Ne doit pas lever d'exception
            comptesService.delete(clientIdentifier, compteIdentifier);

            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit supprimer tous les comptes si demandé")
        void shouldDeleteAllAccountsIfRequested() throws ImtException {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesService.delete(clientIdentifier, compte1.getIdentifier());
            comptesService.delete(clientIdentifier, compte2.getIdentifier());

            // Then
            verify(storageProvider, times(2)).save(any(Client.class));
        }
    }
}
