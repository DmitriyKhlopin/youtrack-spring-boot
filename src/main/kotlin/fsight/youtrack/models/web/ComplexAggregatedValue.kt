package fsight.youtrack.models.web

interface ComplexAggregatedValue

data class ComplexAggregatedValue1(var order: Int?, val key1: String?, val key2: String?, val value: Int) : ComplexAggregatedValue
data class ComplexAggregatedValue2(var order: Int?, val key1: String, val key2: Boolean, val value: Int) : ComplexAggregatedValue
