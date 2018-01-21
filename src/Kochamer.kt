import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DateFormat
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import java.nio.file.StandardOpenOption

/**
 * Kochamer: A simple merge-changelog.md Kotlin script
 * _(Ko)tlin (cha)ngelog (mer)ge script
 *
 * Provide a target folder with changelog files [#changelog] and a destination [#changelogFile].
 */

/** Folder which contains the changelog markdown files*/
private const val changelogDir = "./changelog/"
/** Destination changelog file */
private const val changelogFile = "CANGELOG.md"
private val locale = Locale.GERMAN
private val deletMergedFiles = true

private val newLine = System.lineSeparator()

private fun Topic.formatTopic() = when (this) {
    Topic.public -> "# Public:"
    Topic.private -> "# Private:"
}

private fun releaseNamePattern(buildName: String, date: String) = "$buildName vom $date"

/**
 * You can run me without any params
 */
fun main(args: Array<String>) {
    val changelog = readChangeLogs()
    changelog.appendToFileOnTop(changelogFile)
    if (deletMergedFiles) {
        listReadmeFiles().deleteAll()
        println("Deleted merged files")
    }
    println("New changelog entry:")
    println(changelog)
}

private fun readChangeLogs(): String {
    return listReadmeFiles()
            .map { ChangelogFile(it) }
            .flatMap { readChangelogEntry(it) }
            .collect(Collectors.groupingBy<ChangelogEntry, Topic> { it.topic })
            .formatChangeLog()
}

private fun listReadmeFiles(): Stream<File> {
    val isChangelog = FileSystems.getDefault().getPathMatcher("glob:**.md")
    return Files.list(Paths.get(changelogDir)).filter(isChangelog::matches)
            .map { it.toFile() }
}

enum class Topic { public, private }
data class ChangelogEntry(val topic: Topic = Topic.public, val task: String, val text: String)
data class ChangelogFile(val name: String, val content: List<String>) {
    constructor(file: File) : this(file.name, file.readLines())
}

fun readChangelogEntry(file: ChangelogFile): Stream<ChangelogEntry> {
    val logEntries = arrayListOf<ChangelogEntry>()
    val task = file.name.remove(Regex("\\.md$", RegexOption.IGNORE_CASE))
    var topic = Topic.public

    file.content.forEach { line ->
        var parsedLine = line
        parsedLine = parsedLine.removeSuffix("[$task]").trim()
        if (parsedLine.containsTopic(Topic.public)) {
            parsedLine = parsedLine.remove(Topic.public.toRegex())
            topic = Topic.public
            if (parsedLine.isNotBlank()) {
                logEntries += ChangelogEntry(topic = topic, task = task, text = parsedLine)
            }
        } else if (parsedLine.containsTopic(Topic.private)) {
            topic = Topic.private
            parsedLine = parsedLine.remove(Topic.private.toRegex())
            if (parsedLine.isNotBlank()) {
                logEntries += ChangelogEntry(topic = topic, task = task, text = parsedLine)
            }
        } else {
            logEntries += ChangelogEntry(topic = topic, task = task, text = parsedLine)
        }

    }
    return logEntries.stream()
}

fun String.containsTopic(topic: Topic) = this.contains(regexTopic(topic))

private fun Map<Topic, List<ChangelogEntry>>.formatChangeLog(): String {
    var changeLogEntry = getReleaseName() + newLine + newLine

    fun combineToChangelog(topic: Topic) {
        changeLogEntry += topic.formatTopic() + newLine
        changeLogEntry += this[topic].formatLogEntryList()
        changeLogEntry += newLine
    }
    Topic.values().forEach { combineToChangelog(it) }
    return changeLogEntry
}

private fun List<ChangelogEntry>?.formatLogEntryList() = this?.joinToString(separator = newLine) { it.formatLogEntry() } + newLine

private fun getReleaseName(): String {
    val releaseName = Calendar.getInstance()[Calendar.YEAR].toString() + "." + Calendar.getInstance()[Calendar.WEEK_OF_YEAR].toString()
    val date = DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date())
    return releaseNamePattern(releaseName, date)
}

private fun ChangelogEntry.formatLogEntry(): String = "${this.text} [${this.task}]"

private fun Topic.toRegex() = regexTopic(this)

private fun regexTopic(topic: Topic) = Regex("\\**\\s*#*\\s*${topic.name}:\\s*", RegexOption.IGNORE_CASE)

private fun String.remove(regex: Regex) = this.replace(regex, "")

private fun String.appendToFileOnTop(fileName: String) {
    val fileContent = Files.readAllBytes(Paths.get(fileName))
    Files.write(
            Paths.get(fileName),
            this.toByteArray(),
            StandardOpenOption.WRITE)
    Files.write(
            Paths.get(fileName),
            fileContent,
            StandardOpenOption.APPEND)
}

private fun Stream<File>.deleteAll() = this.forEach { it.delete() }