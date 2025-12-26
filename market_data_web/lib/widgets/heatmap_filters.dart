import 'package:flutter/material.dart';

class HeatmapFilters extends StatelessWidget {
  final String timeFrame;
  final ValueChanged<String?> onTimeFrameChanged;
  final String? percentFilter;
  final ValueChanged<String?> onPercentFilterChanged;
  
  final List<String> timeFrames;
  final List<String> filters;

  const HeatmapFilters({
    super.key,
    required this.timeFrame,
    required this.onTimeFrameChanged,
    required this.percentFilter,
    required this.onPercentFilterChanged,
    this.timeFrames = const ['5M', '10M', '15M', '30M', '1H', '1D'],
    this.filters = const ['Above +5%', '+2 to +5%', '0 to +2%', '0 to -2%', '-2 to -5%', 'Below -5%'],
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      width: double.infinity,
      color: const Color(0xFF2C2C3E),
      child: Wrap(
        spacing: 20,
        runSpacing: 10,
        crossAxisAlignment: WrapCrossAlignment.center,
        alignment: WrapAlignment.spaceBetween,
        children: [
          // Time Frame Dropdown
          DropdownButton<String>(
            value: timeFrame,
            dropdownColor: const Color(0xFF2C2C3E),
            style: const TextStyle(color: Colors.white),
            underline: Container(height: 1, color: Colors.blue),
            items: timeFrames.map((tf) => DropdownMenuItem(value: tf, child: Text(tf))).toList(),
            onChanged: onTimeFrameChanged,
          ),
          
          // Percent Filters
          Wrap(
            spacing: 8,
            children: filters.map((f) {
              final isSelected = percentFilter == f;
              Color color;
                if (f.contains('Above')) color = Colors.green[700]!;
                else if (f.contains('+2')) color = Colors.green[500]!;
                else if (f.contains('0 to +2')) color = Colors.green[300]!;
                else if (f.contains('0 to -2')) color = Colors.red[300]!;
                else if (f.contains('-2 to')) color = Colors.red[500]!;
                else color = Colors.red[900]!;

              return FilterChip(
                label: Text(f, style: const TextStyle(fontSize: 12)),
                selected: isSelected,
                onSelected: (selected) {
                  onPercentFilterChanged(selected ? f : null);
                },
                backgroundColor: Colors.black12,
                selectedColor: color,
                checkmarkColor: Colors.white,
                labelStyle: TextStyle(color: isSelected ? Colors.white : Colors.grey),
                side: BorderSide.none,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }
}
