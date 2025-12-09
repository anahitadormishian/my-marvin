def gitHubName = binding.variables.containsKey('GITHUB_NAME') ? GITHUB_NAME : null
def displayName = binding.variables.containsKey('DISPLAY_NAME') ? DISPLAY_NAME : null

folder('Tools') {
    description('Folder for miscellaneous tools.')
}

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

job('Tools/SEED') {
    description('Generate jobs from the provided GitHub repository and display name')

    parameters {
        stringParam('GITHUB_NAME', '', 'GitHub repository owner/repo_name')
        stringParam('DISPLAY_NAME', '', 'Display name for the job')
    }

    steps {
        shell('cp /var/jenkins_home/job_dsl.groovy job_dsl.groovy')
        dsl {
            external('job_dsl.groovy')
            lookupStrategy('SEED_JOB')
        }
    }
}

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
