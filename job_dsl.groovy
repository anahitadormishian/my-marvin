//
// Retrieve parameters provided by the SEED job
//
def gitHubName = binding.variables.get('GITHUB_NAME')
def displayName = binding.variables.get('DISPLAY_NAME')

//
// STATIC JOB: clone-repository
//
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

//
// DYNAMIC JOB: created by SEED using the parameters
//
if (gitHubName && displayName) {

    def repoUrl = "https://github.com/${gitHubName}"

    job(displayName) {

        // GitHub project URL shown on job page
        properties {
            githubProjectUrl(repoUrl)
        }

        // Git SCM checkout configuration
        scm {
            git {
                remote {
                    github(gitHubName, "https")
                }
                branch("*/main")
            }
        }

        // Check for new commits every minute
        triggers {
            scm("* * * * *")
        }

        wrappers {
            preBuildCleanup()
        }

        // Commands required by the subject
        steps {
            shell("make fclean")
            shell("make")
            shell("make tests_run")
            shell("make clean")
        }
    }
}
