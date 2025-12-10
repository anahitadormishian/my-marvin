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
        dsl {
            external('/var/jenkins_home/job_dsl.groovy')
            lookupStrategy('SEED_JOB')
        }
    }
}

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
