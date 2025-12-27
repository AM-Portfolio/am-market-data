//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class SecurityDTOV1 {
  /// Returns a new [SecurityDTOV1] instance.
  SecurityDTOV1({
    this.symbol,
    this.isin,
    this.sector,
    this.industry,
    this.marketCapValue,
    this.marketCapType,
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
  String? isin;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? sector;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? industry;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  int? marketCapValue;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? marketCapType;

  @override
  bool operator ==(Object other) => identical(this, other) || other is SecurityDTOV1 &&
    other.symbol == symbol &&
    other.isin == isin &&
    other.sector == sector &&
    other.industry == industry &&
    other.marketCapValue == marketCapValue &&
    other.marketCapType == marketCapType;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (symbol == null ? 0 : symbol!.hashCode) +
    (isin == null ? 0 : isin!.hashCode) +
    (sector == null ? 0 : sector!.hashCode) +
    (industry == null ? 0 : industry!.hashCode) +
    (marketCapValue == null ? 0 : marketCapValue!.hashCode) +
    (marketCapType == null ? 0 : marketCapType!.hashCode);

  @override
  String toString() => 'SecurityDTOV1[symbol=$symbol, isin=$isin, sector=$sector, industry=$industry, marketCapValue=$marketCapValue, marketCapType=$marketCapType]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (this.symbol != null) {
      json[r'symbol'] = this.symbol;
    } else {
      json[r'symbol'] = null;
    }
    if (this.isin != null) {
      json[r'isin'] = this.isin;
    } else {
      json[r'isin'] = null;
    }
    if (this.sector != null) {
      json[r'sector'] = this.sector;
    } else {
      json[r'sector'] = null;
    }
    if (this.industry != null) {
      json[r'industry'] = this.industry;
    } else {
      json[r'industry'] = null;
    }
    if (this.marketCapValue != null) {
      json[r'marketCapValue'] = this.marketCapValue;
    } else {
      json[r'marketCapValue'] = null;
    }
    if (this.marketCapType != null) {
      json[r'marketCapType'] = this.marketCapType;
    } else {
      json[r'marketCapType'] = null;
    }
    return json;
  }

  /// Returns a new [SecurityDTOV1] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static SecurityDTOV1? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        requiredKeys.forEach((key) {
          assert(json.containsKey(key), 'Required key "SecurityDTOV1[$key]" is missing from JSON.');
          assert(json[key] != null, 'Required key "SecurityDTOV1[$key]" has a null value in JSON.');
        });
        return true;
      }());

      return SecurityDTOV1(
        symbol: mapValueOfType<String>(json, r'symbol'),
        isin: mapValueOfType<String>(json, r'isin'),
        sector: mapValueOfType<String>(json, r'sector'),
        industry: mapValueOfType<String>(json, r'industry'),
        marketCapValue: mapValueOfType<int>(json, r'marketCapValue'),
        marketCapType: mapValueOfType<String>(json, r'marketCapType'),
      );
    }
    return null;
  }

  static List<SecurityDTOV1> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <SecurityDTOV1>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = SecurityDTOV1.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, SecurityDTOV1> mapFromJson(dynamic json) {
    final map = <String, SecurityDTOV1>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = SecurityDTOV1.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of SecurityDTOV1-objects as value to a dart map
  static Map<String, List<SecurityDTOV1>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<SecurityDTOV1>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = SecurityDTOV1.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
  };
}

