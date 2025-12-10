def gitHubName  = binding.variables.containsKey('GITHUB_NAME') ? GITHUB_NAME : null
def displayName = binding.variables.containsKey('DISPLAY_NAME') ? DISPLAY_NAME : null


job('Tools/clone-repository') {
    description('Clone a repository from a URL')

    parameters {
        stringParam('GIT_REPOSITORY_URL', '', 'Git URL of the repository to clone')
    }

    wrappers {
        preBuildCleanup()
    }

    steps {
        shell('git clone "$GIT_REPOSITORY_URL"')
    }
}

if (gitHubName && displayName) {

    def repoHttpUrl = "https://github.com/${gitHubName}"
    def repoGitUrl  = "${repoHttpUrl}.git"

    job(displayName) {

        properties {
            githubProjectUrl(repoHttpUrl)
        }

        scm {
            git {
                remote {
                    url(repoGitUrl)
                }
                branch('*/main')
            }
        }

        triggers {
            scm('* * * * *')
        }

        wrappers {
            preBuildCleanup()
        }

        steps {
            shell('make fclean')
            shell('make')
            shell('make tests_run')
            shell('make clean')
        }
    }
}
