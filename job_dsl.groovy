def gitHubName = binding.variables.get('GITHUB_NAME')
def displayName = binding.variables.get('DISPLAY_NAME')

if (gitHubName && displayName) {

    def repoUrl = "https://github.com/${gitHubName}"

    job(displayName) {

        properties {
            githubProjectUrl(repoUrl)
        }

        scm {
            git {
                remote {
                    github(gitHubName, "https")
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
}
