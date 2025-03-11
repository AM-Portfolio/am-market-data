{{/*
Expand the name of the chart.
*/}}
{{- define "market-data.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "market-data.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "market-data.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "market-data.labels" -}}
helm.sh/chart: {{ include "market-data.chart" . }}
{{ include "market-data.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "market-data.selectorLabels" -}}
app.kubernetes.io/name: {{ include "market-data.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "market-data.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "market-data.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
PostgreSQL fullname
*/}}
{{- define "market-data.postgresql.fullname" -}}
{{- printf "%s-postgresql" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
PostgreSQL secret name
*/}}
{{- define "market-data.postgresql.secretName" -}}
{{- printf "%s-postgresql" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Kafka bootstrap servers
*/}}
{{- define "market-data.kafka.bootstrapServers" -}}
{{- printf "%s-kafka:9092" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
InfluxDB fullname
*/}}
{{- define "market-data.influxdb.fullname" -}}
{{- printf "%s-influxdb" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
InfluxDB secret name
*/}}
{{- define "market-data.influxdb.secretName" -}}
{{- printf "%s-influxdb-auth" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
