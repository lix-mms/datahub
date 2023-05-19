package com.linkedin.common.urn;

import com.linkedin.data.template.Custom;
import com.linkedin.data.template.DirectCoercer;
import com.linkedin.data.template.TemplateOutputCastException;

import java.net.URISyntaxException;


public final class DataAccessUrn extends Urn {

  public static final String ENTITY_TYPE = "dataAccess";

  private final DatasetUrn _dataset;
  private final DataPlatformPrincipalUrn _principal;

  public DataAccessUrn(DatasetUrn dataset, DataPlatformPrincipalUrn principal) {
    super(ENTITY_TYPE, TupleKey.create(dataset, principal));
    this._dataset = dataset;
    this._principal = principal;
  }

  public DatasetUrn getDatasetEntity() {
    return _dataset;
  }

  public DataPlatformPrincipalUrn getDataPlatformPrincipalEntity() {
    return _principal;
  }

  public static DataAccessUrn createFromString(String rawUrn) throws URISyntaxException {
    return createFromUrn(Urn.createFromString(rawUrn));
  }

  public static DataAccessUrn createFromUrn(Urn urn) throws URISyntaxException {
    if (!"li".equals(urn.getNamespace())) {
      throw new URISyntaxException(urn.toString(), "Urn namespace type should be 'li'.");
    } else if (!ENTITY_TYPE.equals(urn.getEntityType())) {
      throw new URISyntaxException(urn.toString(), "Urn entity type should be " + ENTITY_TYPE + ".");
    } else {
      TupleKey key = urn.getEntityKey();
      if (key.size() != 2) {
        throw new URISyntaxException(urn.toString(), "Invalid number of keys.");
      } else {
        try {
          return new DataAccessUrn(
                  key.getAs(0, DatasetUrn.class),
                  key.getAs(1, DataPlatformPrincipalUrn.class)
          );
        } catch (Exception var3) {
          throw new URISyntaxException(urn.toString(), "Invalid URN Parameter: '" + var3.getMessage());
        }
      }
    }
  }

  public static DataAccessUrn deserialize(String rawUrn) throws URISyntaxException {
    return createFromString(rawUrn);
  }

  static {
    Custom.initializeCustomClass(DatasetUrn.class);
    Custom.initializeCustomClass(DataAccessUrn.class);
    Custom.initializeCustomClass(DataPlatformPrincipalUrn.class);
    Custom.registerCoercer(new DirectCoercer<DataAccessUrn>() {
      public Object coerceInput(DataAccessUrn object) throws ClassCastException {
        return object.toString();
      }

      public DataAccessUrn coerceOutput(Object object) throws TemplateOutputCastException {
        try {
          return DataAccessUrn.createFromString((String) object);
        } catch (URISyntaxException e) {
          throw new TemplateOutputCastException("Invalid URN syntax: " + e.getMessage(), e);
        }
      }
    }, DataAccessUrn.class);
  }
}
