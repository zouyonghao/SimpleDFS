sdfsRead("1\n2\n444\n555")
sdfsMap({ a: String -> a.split("\n") })
sdfsMap({ a: List<String> -> a.map{ it.toInt() } })
sdfsReduce({ a: List<Int> -> a.reduce { i, j -> i + j } })