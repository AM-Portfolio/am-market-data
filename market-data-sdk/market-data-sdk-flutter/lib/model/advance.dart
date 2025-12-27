//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;

class Advance {
  /// Returns a new [Advance] instance.
  Advance({
    this.declines,
    this.advances,
    this.unchanged,
  });

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? declines;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? advances;

  ///
  /// Please note: This property should have been non-nullable! Since the specification file
  /// does not include a default value (using the "default:" property), however, the generated
  /// source code must fall back to having a nullable type.
  /// Consider adding a "default:" property in the specification file to hide this note.
  ///
  String? unchanged;

  @override
  bool operator ==(Object other) => identical(this, other) || other is Advance &&
    other.declines == declines &&
    other.advances == advances &&
    other.unchanged == unchanged;

  @override
  int get hashCode =>
    // ignore: unnecessary_parenthesis
    (declines == null ? 0 : declines!.hashCode) +
    (advances == null ? 0 : advances!.hashCode) +
    (unchanged == null ? 0 : unchanged!.hashCode);

  @override
  String toString() => 'Advance[declines=$declines, advances=$advances, unchanged=$unchanged]';

  Map<String, dynamic> toJson() {
    final json = <String, dynamic>{};
    if (this.declines != null) {
      json[r'declines'] = this.declines;
    } else {
      json[r'declines'] = null;
    }
    if (this.advances != null) {
      json[r'advances'] = this.advances;
    } else {
      json[r'advances'] = null;
    }
    if (this.unchanged != null) {
      json[r'unchanged'] = this.unchanged;
    } else {
      json[r'unchanged'] = null;
    }
    return json;
  }

  /// Returns a new [Advance] instance and imports its values from
  /// [value] if it's a [Map], null otherwise.
  // ignore: prefer_constructors_over_static_methods
  static Advance? fromJson(dynamic value) {
    if (value is Map) {
      final json = value.cast<String, dynamic>();

      // Ensure that the map contains the required keys.
      // Note 1: the values aren't checked for validity beyond being non-null.
      // Note 2: this code is stripped in release mode!
      assert(() {
        requiredKeys.forEach((key) {
          assert(json.containsKey(key), 'Required key "Advance[$key]" is missing from JSON.');
          assert(json[key] != null, 'Required key "Advance[$key]" has a null value in JSON.');
        });
        return true;
      }());

      return Advance(
        declines: mapValueOfType<String>(json, r'declines'),
        advances: mapValueOfType<String>(json, r'advances'),
        unchanged: mapValueOfType<String>(json, r'unchanged'),
      );
    }
    return null;
  }

  static List<Advance> listFromJson(dynamic json, {bool growable = false,}) {
    final result = <Advance>[];
    if (json is List && json.isNotEmpty) {
      for (final row in json) {
        final value = Advance.fromJson(row);
        if (value != null) {
          result.add(value);
        }
      }
    }
    return result.toList(growable: growable);
  }

  static Map<String, Advance> mapFromJson(dynamic json) {
    final map = <String, Advance>{};
    if (json is Map && json.isNotEmpty) {
      json = json.cast<String, dynamic>(); // ignore: parameter_assignments
      for (final entry in json.entries) {
        final value = Advance.fromJson(entry.value);
        if (value != null) {
          map[entry.key] = value;
        }
      }
    }
    return map;
  }

  // maps a json object with a list of Advance-objects as value to a dart map
  static Map<String, List<Advance>> mapListFromJson(dynamic json, {bool growable = false,}) {
    final map = <String, List<Advance>>{};
    if (json is Map && json.isNotEmpty) {
      // ignore: parameter_assignments
      json = json.cast<String, dynamic>();
      for (final entry in json.entries) {
        map[entry.key] = Advance.listFromJson(entry.value, growable: growable,);
      }
    }
    return map;
  }

  /// The list of required keys that must be present in a JSON.
  static const requiredKeys = <String>{
  };
}

