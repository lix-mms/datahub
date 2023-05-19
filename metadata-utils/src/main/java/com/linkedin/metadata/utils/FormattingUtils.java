package com.linkedin.metadata.utils;

import com.google.common.collect.MapDifference;

public class FormattingUtils {

  public static String formatMapDifference(MapDifference<?, ?> mapDifference) {
    final var stringBuilder = new StringBuilder();
    stringBuilder.append("Changes:\n");
    if (!mapDifference.entriesOnlyOnLeft().isEmpty()) {
      mapDifference.entriesOnlyOnLeft().forEach((k, v) ->
          stringBuilder.append(String.format("- [Removed] %s: %s\n", k, v)));
    }
    if (!mapDifference.entriesOnlyOnRight().isEmpty()) {
      mapDifference.entriesOnlyOnRight().forEach((k, v) ->
          stringBuilder.append(String.format("- [Added] %s: %s\n", k, v)));
    }
    if (!mapDifference.entriesDiffering().isEmpty()) {
      mapDifference.entriesDiffering().forEach((k, vDiff) ->
          stringBuilder.append(String.format("- [Modified] %s: %s -> %s\n", k, vDiff.leftValue(), vDiff.rightValue())));
    }
    return stringBuilder.toString();
  }
}
