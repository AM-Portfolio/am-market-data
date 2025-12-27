class SecuritySearchRequest {
  final List<String>? queries;
  final List<String>? isins;
  final List<String>? exchanges;
  final List<String>? segments;
  final List<String>? instrumentTypes;
  final String provider;

  SecuritySearchRequest({
    this.queries,
    this.isins,
    this.exchanges,
    this.segments,
    this.instrumentTypes, 
    required this.provider,
  });
}
