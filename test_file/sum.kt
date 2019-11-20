sdfsRead("abc/numberFile")
sdfsMap({ a: String -> a.split("\n") })
sdfsMap({ a: List<String> -> a.filter{ it.isNotEmpty() }.map{ it.toInt() } })
sdfsShuffle { a:Int -> a / 10000 }
sdfsReduce({ a: List<Int> -> a.reduce { i, j -> i + j } })