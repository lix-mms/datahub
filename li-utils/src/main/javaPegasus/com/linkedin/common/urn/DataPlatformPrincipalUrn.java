package com.linkedin.common.urn;

import com.linkedin.data.template.Custom;
import com.linkedin.data.template.DirectCoercer;
import com.linkedin.data.template.TemplateOutputCastException;

import java.net.URISyntaxException;


public final class DataPlatformPrincipalUrn extends Urn {

  public static final String ENTITY_TYPE = "dataPlatformPrincipal";

  private final DataPlatformUrn _platform;
  private final String _principal;

  public DataPlatformPrincipalUrn(DataPlatformUrn platform, String _principal) {
    super(ENTITY_TYPE, TupleKey.create(platform, _principal));
    this._platform = platform;
    this._principal = _principal;
  }

  public DataPlatformUrn getPlatformEntity() {
    return _platform;
  }

  public String getPrincipal() {
    return _principal;
  }

  public static DataPlatformPrincipalUrn createFromString(String rawUrn) throws URISyntaxException {
    return createFromUrn(Urn.createFromString(rawUrn));
  }

  public static DataPlatformPrincipalUrn createFromUrn(Urn urn) throws URISyntaxException {
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
          return new DataPlatformPrincipalUrn(
                  key.getAs(0, DataPlatformUrn.class),
                  key.getAs(1, String.class));
        } catch (Exception var3) {
          throw new URISyntaxException(urn.toString(), "Invalid URN Parameter: '" + var3.getMessage());
        }
      }
    }
  }

  public static DataPlatformPrincipalUrn deserialize(String rawUrn) throws URISyntaxException {
    return createFromString(rawUrn);
  }

  static {
    Custom.initializeCustomClass(DataPlatformUrn.class);
    Custom.initializeCustomClass(DataPlatformPrincipalUrn.class);
    Custom.registerCoercer(new DirectCoercer<DataPlatformPrincipalUrn>() {
      public Object coerceInput(DataPlatformPrincipalUrn object) throws ClassCastException {
        return object.toString();
      }

      public DataPlatformPrincipalUrn coerceOutput(Object object) throws TemplateOutputCastException {
        try {
          return DataPlatformPrincipalUrn.createFromString((String) object);
        } catch (URISyntaxException e) {
          throw new TemplateOutputCastException("Invalid URN syntax: " + e.getMessage(), e);
        }
      }
    }, DataPlatformPrincipalUrn.class);
  }
}
