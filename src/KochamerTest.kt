import org.junit.jupiter.api.Test
import java.util.stream.Collectors
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.expect

class KochamerTest {

    @Test
    fun containsTopicPrivate() {
        assertTrue { "private:".containsTopic(Topic.private) }
        assertTrue { "Private:".containsTopic(Topic.private) }
        assertTrue { "#private:".containsTopic(Topic.private) }
        assertTrue { "# private:".containsTopic(Topic.private) }
        assertTrue { "# private: ".containsTopic(Topic.private) }

        assertFalse { "private".containsTopic(Topic.private) }
    }

    @Test
    fun containsTopicPublic() {
        assertTrue { "public:".containsTopic(Topic.public) }
        assertTrue { "Public:".containsTopic(Topic.public) }
        assertTrue { "#public:".containsTopic(Topic.public) }
        assertTrue { "# public:".containsTopic(Topic.public) }
        assertTrue { "# public: ".containsTopic(Topic.public) }

        assertFalse { "public".containsTopic(Topic.public) }
    }

    @Test
    fun defaultPublic() {
        val parsed = readChangeLog("ITS-001.md", listOf("* I'am default public"))

        expect(parsed, { listOf(ChangelogEntry(Topic.public, "ITS-001", "* I'am default public")) })
    }

    @Test
    fun publicInOneLine() {
        val parsed = readChangeLog("ITS-001.md", listOf("# Public: * I'am a public one liner"))
        expect(parsed, { listOf(ChangelogEntry(Topic.public, "ITS-001", "* I'am a public one liner")) })
    }

    @Test
    fun privateInOneLine() {
        val parsed = readChangeLog("ITS-001.md", listOf("# Private: * I'am a private one liner"))
        expect(parsed, { listOf(ChangelogEntry(Topic.private, "ITS-001", "* I'am a private one liner")) })
    }

    @Test
    fun privateSection() {
        val parsed = readChangeLog("ITS-001.md", listOf(
                "# Private:"
                , "* I'am a private section"))
        expect(parsed, { listOf(ChangelogEntry(Topic.private, "ITS-001", "* I'am a private section")) })
    }

    @Test
    fun publicSection() {
        val parsed = readChangeLog("ITS-001.md", listOf(
                "# Public:"
                , "* I'am a public section"))
        expect(parsed, { listOf(ChangelogEntry(Topic.public, "ITS-001", "* I'am a public section")) })
    }

    @Test
    fun privateAndPublicSection() {
        val parsed = readChangeLog("ITS-001.md", listOf(
                "# Private:",
                "* I'am a private section multi line 1",
                "* I'am a private section multi line 2",
                "# Public:",
                "* I'am a public section multi line 1",
                "* I'am a public section multi line 2"))
        expect(parsed, {
            listOf(
                    ChangelogEntry(Topic.private, "ITS-001", "* I'am a private section multi line 1"),
                    ChangelogEntry(Topic.private, "ITS-001", "* I'am a private section multi line 2"),
                    ChangelogEntry(Topic.public, "ITS-001", "* I'am a public section multi line 1"),
                    ChangelogEntry(Topic.public, "ITS-001", "* I'am a public section multi line 2")
            )
        })
    }

    @Test
    fun publicAndPrivateSection() {
        val parsed = readChangeLog("ITS-001.md", listOf(
                "# Public:",
                "* I'am a public section multi line 1",
                "* I'am a public section multi line 2",
                "# Private:",
                "* I'am a private section multi line 1",
                "* I'am a private section multi line 2")
        )
        expect(parsed, {
            listOf(
                    ChangelogEntry(Topic.public, "ITS-001", "* I'am a public section multi line 1"),
                    ChangelogEntry(Topic.public, "ITS-001", "* I'am a public section multi line 2"),
                    ChangelogEntry(Topic.private, "ITS-001", "* I'am a private section multi line 1"),
                    ChangelogEntry(Topic.private, "ITS-001", "* I'am a private section multi line 2")
            )
        })
    }

    @Test
    fun alreadyContainsTask() {
        val parsed = readChangeLog("ITS-001.md", listOf("* I already contained my task [ITS-001]"))
        expect(parsed, { listOf(ChangelogEntry(Topic.public, "ITS-001", "* I already contained my task")) })
    }

    private fun readChangeLog(fileName: String, fileContent: List<String>): List<ChangelogEntry>? {
        return readChangelogEntry(ChangelogFile(fileName, fileContent)).collect(Collectors.toList())
    }

}