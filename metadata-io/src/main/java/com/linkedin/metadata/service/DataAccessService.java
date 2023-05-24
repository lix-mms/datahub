package com.linkedin.metadata.service;

import com.datahub.authentication.Authentication;
import com.google.common.collect.Maps;
import com.linkedin.common.CorpuserUrnArray;
import com.linkedin.common.Owner;
import com.linkedin.common.Ownership;
import com.linkedin.common.urn.*;
import com.linkedin.dataaccess.DataAccessParties;
import com.linkedin.dataaccess.DataAccessProperties;
import com.linkedin.dataaccess.DataAccessStatus;
import com.linkedin.dataaccess.DataAccessStatusInfo;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.key.DataAccessKey;
import com.linkedin.metadata.utils.EntityKeyUtils;
import com.linkedin.metadata.utils.FormattingUtils;
import com.linkedin.mxe.MetadataChangeProposal;
import com.linkedin.r2.RemoteInvocationException;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.linkedin.metadata.Constants.*;
import static com.linkedin.metadata.utils.GenericRecordUtils.serializeAspect;


public class DataAccessService {
  private final EntityClient _entityClient;

  private final EntityService _entityService;

  public DataAccessService(@Nonnull EntityClient entityClient,
                           @Nonnull EntityService entityService
  ) {
    Objects.requireNonNull(entityClient, "entityClient must not be null!");
    _entityClient = entityClient;
    _entityService = entityService;
  }

  @SneakyThrows(RemoteInvocationException.class)
  public String createDataAccess(@Nonnull final DataAccessKey dataAccessKey,
                                 @Nonnull final DataAccessProperties dataAccessProperties,
                                 @Nonnull final DataAccessStatusInfo dataAccessStatusInfo,
                                 final Authentication authentication) throws URISyntaxException {
    Objects.requireNonNull(dataAccessKey, "DataAccessKey must not be null");
    Objects.requireNonNull(dataAccessProperties, "dataAccessProperties must not be null");
    Objects.requireNonNull(dataAccessStatusInfo, "dataAccessStatusInfo must not be null");

    final var owners = getOwnerCorpUsersOfDataset(dataAccessKey.getDataset()).collect(Collectors.toList());
    final var dataAccessParties = new DataAccessParties();
    dataAccessParties.setRequester(CorpuserUrn.createFromString(authentication.getActor().toUrnStr()));
    dataAccessParties.setAuthorizedApprovers(new CorpuserUrnArray(owners));

    publishDataAccess(EntityKeyUtils.convertEntityKeyToUrn(dataAccessKey, DATA_ACCESS_ENTITY_NAME), dataAccessProperties, dataAccessStatusInfo, dataAccessParties, authentication);
    return EntityKeyUtils.convertEntityKeyToUrn(dataAccessKey, DATA_ACCESS_ENTITY_NAME).toString();
  }

  @SneakyThrows({RemoteInvocationException.class, CloneNotSupportedException.class})
  public String updateDataAccess(@Nonnull final DataAccessUrn urn,
                                 @Nullable final DataAccessProperties propertiesFromInput,
                                 @Nonnull final DataAccessStatusInfo statusInfoFromInput,
                                 final Authentication authentication) throws URISyntaxException {
    Objects.requireNonNull(urn, "urn must not be null");
    Objects.requireNonNull(statusInfoFromInput, "statusInfoFromInput must not be null");

    if (!_entityClient.exists(urn, authentication)) {
      throw new IllegalArgumentException("Data access not found.");
    }

    final var existingDataAccessResponse = _entityClient.getV2(
        DATA_ACCESS_ENTITY_NAME,
        urn,
        Set.of(DATA_ACCESS_PROPERTIES_ASPECT_NAME,
            DATA_ACCESS_STATUS_INFO_ASPECT_NAME,
            DATA_ACCESS_PARTIES_ASPECT_NAME),
        authentication);

    final var existingKeyEnvAsp = existingDataAccessResponse.getAspects().get(DATA_ACCESS_KEY_ASPECT_NAME);
    final var key = new DataAccessKey(existingKeyEnvAsp.getValue().data());

    DataAccessParties parties = null;

    final var newStatusInfo = statusInfoFromInput.copy();
    // verify if current user is allowed to approve request
    if (newStatusInfo.getStatus() == DataAccessStatus.APPROVED ||
        newStatusInfo.getStatus() == DataAccessStatus.PROVISIONED) {
      final var actorUrnStr = authentication.getActor().toUrnStr();
      if (getOwnerCorpUsersOfDataset(key.getDataset())
          .noneMatch(u -> u.toString().equals(actorUrnStr))
      ) {
        throw new IllegalArgumentException("Only owners are allowed to approve data access requests.");
      }

      final var existingPartiesEnvAsp = existingDataAccessResponse.getAspects().get(DATA_ACCESS_PARTIES_ASPECT_NAME);
      // ensure grantor is set
      parties = new DataAccessParties(existingPartiesEnvAsp.getValue().data());
      parties.setGrantor(CorpuserUrn.createFromString(actorUrnStr));
    }

    // add audit message for status change
    final var existingPropertiesEnvAsp = existingDataAccessResponse.getAspects().get(DATA_ACCESS_PROPERTIES_ASPECT_NAME);
    final var propertiesDiffMessage = propertiesFromInput == null ? "" :
        "* " + FormattingUtils.formatMapDifference(Maps.difference(
            existingPropertiesEnvAsp.getValue().data(),
            propertiesFromInput.data()));
    final var existingStatusInfoEnvAsp = existingDataAccessResponse.getAspects().get(DATA_ACCESS_STATUS_INFO_ASPECT_NAME);
    final var existingStatusInfo = new DataAccessStatusInfo(existingStatusInfoEnvAsp.getValue().data());
    newStatusInfo.getAuditStamp().setMessage(
        String.format(
            "<SYSTEM MESSAGE>\n* Status change from %s to %s\n%s",
            existingStatusInfo.getStatus(),
            newStatusInfo.getStatus(),
            propertiesDiffMessage));

    publishDataAccess(urn, propertiesFromInput, newStatusInfo, parties, authentication);
    return urn.toString();
  }

  void publishDataAccess(@Nonnull final Urn urn,
                         @Nullable final DataAccessProperties dataAccessProperties,
                         @Nonnull final DataAccessStatusInfo dataAccessStatusInfo,
                         @Nullable final DataAccessParties dataAccessParties,
                         final Authentication authentication) throws RemoteInvocationException {
    Objects.requireNonNull(urn, "urn must not be null");
    Objects.requireNonNull(dataAccessStatusInfo, "dataAccessStatusInfo must not be null");

    if (dataAccessProperties != null) {
      final MetadataChangeProposal proposalProperties = new MetadataChangeProposal();
      proposalProperties.setEntityUrn(urn);
      proposalProperties.setEntityType(DATA_ACCESS_ENTITY_NAME);
      proposalProperties.setAspectName(DATA_ACCESS_PROPERTIES_ASPECT_NAME);
      proposalProperties.setAspect(serializeAspect(dataAccessProperties));
      proposalProperties.setChangeType(ChangeType.UPSERT);
      _entityClient.ingestProposal(proposalProperties, authentication, false);
    }

    if (dataAccessParties != null) {
      final MetadataChangeProposal proposalProperties = new MetadataChangeProposal();
      proposalProperties.setEntityUrn(urn);
      proposalProperties.setEntityType(DATA_ACCESS_ENTITY_NAME);
      proposalProperties.setAspectName(DATA_ACCESS_PARTIES_ASPECT_NAME);
      proposalProperties.setAspect(serializeAspect(dataAccessParties));
      proposalProperties.setChangeType(ChangeType.UPSERT);
      _entityClient.ingestProposal(proposalProperties, authentication, false);
    }

    final MetadataChangeProposal proposalStatusInfo = new MetadataChangeProposal();
    proposalStatusInfo.setEntityUrn(urn);
    proposalStatusInfo.setEntityType(DATA_ACCESS_ENTITY_NAME);
    proposalStatusInfo.setAspectName(DATA_ACCESS_STATUS_INFO_ASPECT_NAME);
    proposalStatusInfo.setAspect(serializeAspect(dataAccessStatusInfo));
    proposalStatusInfo.setChangeType(ChangeType.UPSERT);
    _entityClient.ingestProposal(proposalStatusInfo, authentication, false);
  }

  private Stream<CorpuserUrn> getOwnerCorpUsersOfDataset(DatasetUrn datasetUrn) throws RemoteInvocationException, URISyntaxException {
    // get owners of entity
    //noinspection DataFlowIssue
    final var ownershipResponseV2 = _entityClient.getV2(
        DATASET_ENTITY_NAME,
        datasetUrn,
        Set.of(OWNERSHIP_ASPECT_NAME),
        null);
    if (ownershipResponseV2 == null) {
      throw new IllegalArgumentException("No owners of dataset to approve data access requests.");
    }

    final var ownership = ownershipResponseV2.getAspects().get(OWNERSHIP_ASPECT_NAME);
    if (ownership == null) {
      throw new IllegalArgumentException("No owners of dataset to approve data access requests.");
    }

    // limitation: ignore CorpGroup as owner for now
    return new Ownership(ownership.getValue().data())
        .getOwners()
        .stream()
        .map(Owner::getOwner)
        .filter(o -> o.getEntityType().equals(CORP_USER_ENTITY_NAME))
        .map(o -> {
          try {
            return CorpuserUrn.createFromUrn(o);
          } catch (URISyntaxException e) {
            throw new RuntimeException(e);
          }
        });
  }

  public List<DataAccessStatusInfo> getDataAccessStatusInfoHistory(Urn urn, int start, int count) {
    final var records = _entityService.listAspectInLatestVersions(urn, DATA_ACCESS_STATUS_INFO_ASPECT_NAME, start, count);
    return records.stream().map(recordTemplate -> new DataAccessStatusInfo(recordTemplate.data())).collect(Collectors.toList());
  }
}
