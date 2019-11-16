// binding "file" as String

import cn.edu.tsinghua.sdfs.user.program.ScriptRunner

val result = file.split("\n").map { it.toInt() }.reduce { a, b -> a + b }

"abc/number"