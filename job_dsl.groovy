//
// 1️⃣ Safely get parameters from SEED
//
def gitHubName  = binding.variables.containsKey('GITHUB_NAME') ? GITHUB_NAME : null
def displayName = binding.variables.containsKey('DISPLAY_NAME') ? DISPLAY_NAME : null

//
// 2️⃣ STATIC JOB: Tools/clone-repository
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
// 3️⃣ DYNAMIC JOB: created by SEED (only if params are given)
//
if (gitHubName && displayName) {

    def repoHttpUrl = "https://github.com/${gitHubName}"
    def repoGitUrl  = "${repoHttpUrl}.git"

    job(displayName) {

        // GitHub project URL shown on the job page
        properties {
            githubProjectUrl(repoHttpUrl)
        }

        // SCM: checkout from GitHub
        scm {
            git {
                remote {
                    url(repoGitUrl)
                }
                branch('*/main')
            }
        }

        // Poll SCM every minute for new commits
        triggers {
            scm('* * * * *')
        }

        wrappers {
            preBuildCleanup()
        }

        // 🧱 BUILD STEPS - "step by step" as your tutor wants
        steps {
            shell('make fclean')
            shell('make')
            shell('make tests_run')
            shell('make clean')
        }
    }
}
