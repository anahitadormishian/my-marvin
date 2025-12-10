// Retrieve parameters when SEED job triggers this script
def gitHubName = binding.variables.containsKey('GITHUB_NAME') ? GITHUB_NAME : null
def displayName = binding.variables.containsKey('DISPLAY_NAME') ? DISPLAY_NAME : null

// -----------------------------
// Folder: Tools
// -----------------------------
folder('Tools') {
    description('Folder for miscellaneous tools.')
}

// -----------------------------
// Job: clone-repository
// -----------------------------
job('Tools/clone-repository') {
    description('Clone a repository from the provided URL')

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

// -----------------------------
// Job: SEED (main generator job)
// -----------------------------
job('Tools/SEED') {
    description('Generate jobs from the provided GitHub repository and display name')

    parameters {
        stringParam(
            'GITHUB_NAME',
            '',
            'GitHub repository owner/repo_name (e.g.: "EpitechIT31000/chocolatine")'
        )
        stringParam(
            'DISPLAY_NAME',
            '',
            'Display name for the job'
        )
    }

    steps {
        // Copy DSL script into workspace
        shell('cp /var/jenkins_home/job_dsl.groovy job_dsl.groovy')

        // Execute DSL from workspace
        dsl {
            external('job_dsl.groovy')
            lookupStrategy('SEED_JOB')
        }
    }
}

// -----------------------------
// Generated job when parameters are provided
// -----------------------------
if (gitHubName && displayName) {

    def repoUrl = "https://github.com/${gitHubName}"

    job("${displayName}") {

        properties {
            githubProjectUrl(repoUrl)
        }

        scm {
            git {
                remote {
                    github(gitHubName, 'https')
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
