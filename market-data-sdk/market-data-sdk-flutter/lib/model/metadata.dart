//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class Metadata {
  /// Returns a new [Metadata] instance.
  Metadata({
    this.symbol,
    this.companyName,
    this.industry,
    this.activeSeries = const [],
    this.isin,
  });

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? symbol;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? companyName;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? industry;

  List<String> activeSeries;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? isin;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Metadata &&
    other.symbol == symbol &&
    other.companyName == companyName &&
    other.industry == industry &&
    _deepEquality.equals(other.activeSeries, activeSeries) &&
    other.isin == isin;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (symbol == null ? 0 : symbol!.hashCode) +
    (companyName == null ? 0 : companyName!.hashCode) +
    (industry == null ? 0 : industry!.hashCode) +
    (activeSeries.hashCode) +
    (isin == null ? 0 : isin!.hashCode);

  @override
  String toString() => 'Metadata[symbol=$symbol, companyName=$companyName, industry=$industry, activeSeries=$activeSeries, isin=$isin]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (this.symbol != null) {
      json[r'symbol'] = this.symbol;
    } else {
      json[r'symbol'] = null;
    }
    if (this.companyName != null) {
      json[r'companyName'] = this.companyName;
    } else {
      json[r'companyName'] = null;
    }
    if (this.industry != null) {
      json[r'industry'] = this.industry;
    } else {
      json[r'industry'] = null;
    }
      json[r'activeSeries'] = this.activeSeries;
    if (this.isin != null) {
      json[r'isin'] = this.isin;
    } else {
      json[r'isin'] = null;
    }
    return json;
  }

  /// Returns a new [Metadata] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static Metadata? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        requiredKeys.forEach((key) {
          assert(json.containsKey(key), 'Required key "Metadata[$key]" is missing from JSON.');
          assert(json[key] != null, 'Required key "Metadata[$key]" has a null value in JSON.');
        });
        return true;
      }());

      return Metadata(
        symbol: mapValueOfType<String>(json, r'symbol'),
        companyName: mapValueOfType<String>(json, r'companyName'),
        industry: mapValueOfType<String>(json, r'industry'),
        activeSeries: json[r'activeSeries'] is Iterable
            ? (json[r'activeSeries'] as Iterable).cast<String>().toList(growable: false)
            : const [],
        isin: mapValueOfType<String>(json, r'isin'),
      );
    }
    return null;
  }

  static List<Metadata> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <Metadata>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = Metadata.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, Metadata> mapFromJson(dynamic json) {
    final map = <String, Metadata>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = Metadata.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of Metadata-objects as value to a dart map
  static Map<String, List<Metadata>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<Metadata>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = Metadata.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
  };
}

