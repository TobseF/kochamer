# Kochamer: A merge changelog.md Kotlin script
_(**Ko**)tlin (**cha**)ngelog (**Mer**)ge script_

This script collects all markdown based changelog files in a given folder and inserts them into an existing `CHANGELOG.md`.
It's used for _Git_ workflows with separated changelog entries to avoid merge conflicts.
Each story/branch can provide its own mini-changelog. And before a release you can easily merge them together.
In addition it supports `public` and `private` sections.

## Problem
We maintain our changelog for our product in the git repository next to the code.
Every branch is based on a user story with a corresponding log entry.
The changelog is one big file with the newest entry in the first line, the `CHANGELOG.md`.
Now whenever we merge different branches together, merge conflicts are almost inevitable.
Even the [`CHANGELOG merge=union`](https://about.gitlab.com/2015/02/10/gitlab-reduced-merge-conflicts-by-90-percent-with-changelog-placeholders/)
didn't help to resolve these conflicts automatically.

## Solution
Provide for every story/branch a separate changelog file named by the Jira Task (e.g. `S-4203.md`).
Merge branches easily without any conflicts on a changelog.
Before tag and release of the master branch run the `kochamer.kt` script to:

1. Read all changelog files in a given folder
2. Group them by `public` and `private` sections
3. Add missing task identifiers based on the filename
4. Generate a release title based on the current date
5. Insert the new changelog entry into the existing `CHANGELOG.md`
6. Delete all merged changelog files

## Example
Consider three files in the folder `changelog` and our existing `CHANGELOG.md` in the main dir:

* `CHANGELOG.md`
``` markdown
# 2018.01 of 05.01.2018

## public:
* Added stardust to the login screen. [S-4101]
* Added cookie based auto login. [S-4100]
* ....
```

* `changelog/S-4201.md`
``` markdown
public:
* We added a tin opener in the tools section, yeah!

private:
* Added an admin function to use the tin opener to view on private user data. 
```
* `changelog/S-4203.md`
``` markdown
private: * Fixed the bug which allowed user with umlauts in their names to login without password. 
```
* `changelog/S-4207.md`
``` markdown
public: * The chat now supports the unicorn emoji.
private: * Removed the middle-finger emoji. 
```

After running `kochamer.kt` the `CHANGELOG.md` will be:
``` markdown
# 2018.04 of 26.01.2018

## public:
* We added a tin opener in the tools section, year! [S-4201]
* The chat now supports the unicorn emoji. [S-4207]

## private:
* Added an admin function to use the tin opener to view on private user data. [S-4203]
* Fixed the bug which allowed user with umlauts in their names to login without password. [S-4203]
* Removed the middle-finger emoji. [S-4207]

# 2018.01 of 05.01.2018

## public:
* Added stardust to the login screen. [S-4101]
* Added cookie based auto login. [S-4100]
* ....
```

View the [KochamerTest.tk](src/KochamerTest.tk) for details.

### Alternatives
* [gnulib git-merge-changelog](https://gnu.wildebeest.org/blog/mjw/2012/03/16/automagically-merging-changelog-files-with-mercurial-or-git/)

### FAQ
* How can I run 'Kochamer'?  
  Run it from IntelliJ with right-click __run/Kochamer.TK__
* Can I change the date formatter?  
  Yes, see  `locale = Locale.GERMAN`
* Kochamer isn't a real Kotlin script, right?  
  Yes, it is simple Kotlin code. You can change the file extension to `.kts` to run it as a script.
  But this way you don't have the option to debug or test the code.


