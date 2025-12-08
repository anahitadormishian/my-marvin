def repoUrl = "https://github.com/${GITHUB_NAME}"

job("${DISPLAY_NAME}") {
    properties {
        githubProjectUrl(repoUrl)
    }

    scm {
        git {
            remote {
                github(GITHUB_NAME, 'https')
            }
            branch("*/main")
        }
    }

    triggers {
        scm("* * * * *")
    }

    wrappers {
        preBuildCleanup()
    }

    steps {
        shell("make fclean")
        shell("make")
        shell("make tests_run")
        shell("make clean")
    }
}
