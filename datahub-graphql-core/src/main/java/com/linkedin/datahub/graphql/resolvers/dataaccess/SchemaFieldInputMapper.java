package com.linkedin.datahub.graphql.resolvers.dataaccess;

import com.linkedin.schema.*;

import javax.annotation.Nonnull;

public class SchemaFieldInputMapper {

  public static com.linkedin.datahub.graphql.generated.SchemaFieldDataType mapPdlTypeToGqlType(@Nonnull final com.linkedin.schema.SchemaFieldDataType dataTypeUnion) {
    final com.linkedin.schema.SchemaFieldDataType.Type type = dataTypeUnion.getType();
    if (type.isBytesType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.BYTES;
    } else if (type.isFixedType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.FIXED;
    } else if (type.isBooleanType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.BOOLEAN;
    } else if (type.isStringType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.STRING;
    } else if (type.isNumberType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.NUMBER;
    } else if (type.isDateType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.DATE;
    } else if (type.isTimeType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.TIME;
    } else if (type.isEnumType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.ENUM;
    } else if (type.isNullType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.NULL;
    } else if (type.isArrayType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.ARRAY;
    } else if (type.isMapType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.MAP;
    } else if (type.isRecordType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.STRUCT;
    } else if (type.isUnionType()) {
      return com.linkedin.datahub.graphql.generated.SchemaFieldDataType.UNION;
    } else {
      throw new RuntimeException(String.format("Unrecognized SchemaFieldDataType provided %s",
          type.memberType().toString()));
    }
  }

  public static SchemaFieldDataType mapGqlTypeToPdlType(@Nonnull final com.linkedin.datahub.graphql.generated.SchemaFieldDataType dataType) {
    SchemaFieldDataType.Type type;
    switch (dataType) {
      case BOOLEAN:
        type = SchemaFieldDataType.Type.create(new BooleanType());
        break;
      case FIXED:
        type = SchemaFieldDataType.Type.create(new FixedType());
        break;
      case STRING:
        type = SchemaFieldDataType.Type.create(new StringType());
        break;
      case BYTES:
        type = SchemaFieldDataType.Type.create(new BytesType());
        break;
      case NUMBER:
        type = SchemaFieldDataType.Type.create(new NumberType());
        break;
      case DATE:
        type = SchemaFieldDataType.Type.create(new DateType());
        break;
      case TIME:
        type = SchemaFieldDataType.Type.create(new TimeType());
        break;
      case ENUM:
        type = SchemaFieldDataType.Type.create(new EnumType());
        break;
      case NULL:
        type = SchemaFieldDataType.Type.create(new NullType());
        break;
      case MAP:
        type = SchemaFieldDataType.Type.create(new MapType());
        break;
      case ARRAY:
        type = SchemaFieldDataType.Type.create(new ArrayType());
        break;
      case UNION:
        type = SchemaFieldDataType.Type.create(new UnionType());
        break;
      case STRUCT:
        type = SchemaFieldDataType.Type.create(new RecordType());
        break;
      default:
        throw new IllegalArgumentException("Unknown type");
    }
    return new SchemaFieldDataType().setType(type);
  }
}
