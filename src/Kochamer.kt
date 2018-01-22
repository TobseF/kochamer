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
 *
 * For details, samples and tests see also: https://github.com/TobseF/kochamer
 */

/** Folder which contains the changelog markdown files*/
private val changelogDir = "./changelog/"
/** Destination changelog file */
private val changelogFile = "./CHANGELOG.md"
private val locale = Locale.GERMAN
private val deleteMergedFiles = true


fun formatTopic(topic: Topic) = when (topic) {
    Topic.public -> "## Public:"
    Topic.private -> "## Private:"
}

fun releaseNamePattern(buildName: String, date: String) = "$buildName vom $date"

/**
 * Runnable without any params.
 * For script config, see the vals above.
 *
 * To use as script rename file to `.kts` add constructor call: `script.main()`
 */
fun main(args: Array<String>) {
    val changelog = readChangeLogs()
    changelog.appendToFileOnTop(changelogFile)
    if (deleteMergedFiles) {
        listReadmeFiles().deleteAll()
        println("Deleted merged files")
    }
    println("New changelog entry:")
    println(changelog)
}

fun readChangeLogs(): String {
    return listReadmeFiles()
            .map { ChangelogFile(it) }
            .flatMap { readChangelogEntry(it) }
            .collect(Collectors.groupingBy<ChangelogEntry, Topic> { it.topic })
            .formatChangeLog()
}

fun listReadmeFiles(): Stream<File> {
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

fun Map<Topic, List<ChangelogEntry>>.formatChangeLog(): String {
    var changeLogEntry = getReleaseName() + System.lineSeparator() + System.lineSeparator()

    fun combineToChangelog(topic: Topic) {
        if (this[topic] != null) {
            changeLogEntry += formatTopic(topic) + System.lineSeparator()
            changeLogEntry += this[topic].formatLogEntryList()
            changeLogEntry += System.lineSeparator()
        }
    }
    Topic.values().forEach { combineToChangelog(it) }
    return changeLogEntry
}

fun List<ChangelogEntry>?.formatLogEntryList() = this?.joinToString(separator = System.lineSeparator()) { it.formatLogEntry() } + System.lineSeparator()

fun getReleaseName(): String {
    val releaseName = Calendar.getInstance()[Calendar.YEAR].toString() + "." + Calendar.getInstance()[Calendar.WEEK_OF_YEAR].toString()
    val date = DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date())
    return releaseNamePattern(releaseName, date)
}

fun ChangelogEntry.formatLogEntry(): String = "${this.text} [${this.task}]"

fun Topic.toRegex() = regexTopic(this)

fun regexTopic(topic: Topic) = Regex("\\**\\s*#*\\s*${topic.name}:\\s*", RegexOption.IGNORE_CASE)

fun String.remove(regex: Regex) = this.replace(regex, "")

fun String.appendToFileOnTop(fileName: String) {
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

fun Stream<File>.deleteAll() = this.forEach { it.delete() }