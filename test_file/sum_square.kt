sdfsRead("abc/number")
sdfsMap({ a: String -> a.split("\n") })
sdfsMap({ a: List<String> -> a.filter{ it.isNotEmpty() }.map{ it.toDouble() * it.toDouble() } })
sdfsShuffle { a:Int -> a / (10000*1000) }
sdfsReduce({ a: List<Int> -> a.reduce { i, j -> i + j } })