//
// STATIC FOLDER: Tools
//
folder('Tools') {
    description('Folder for miscellaneous tools.')
}

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
    // No triggers → manual only
}

//
// STATIC JOB: SEED
//
job('Tools/SEED') {
    description('Seed job to generate other jobs')

    parameters {
        stringParam('GITHUB_NAME', '', 'GitHub repository owner/repo_name (e.g. EpitechIT31000/chocolatine)')
        stringParam('DISPLAY_NAME', '', 'Display name for the job')
    }

    steps {
        // Execute the DSL script itself
        dsl {
            external('job_dsl.groovy')
            removeAction('DELETE') // optional: keep jobs clean
        }
    }
    // No triggers → manual only
}

//
// DYNAMIC JOBS: created by SEED
//
if (gitHubName && displayName) {
    job(displayName) {
        description("Job generated from SEED for ${gitHubName}")

        // GitHub project + SCM in one go
        scm {
            github(gitHubName, 'main')
        }

        properties {
            githubProjectUrl("https://github.com/${gitHubName}")
        }

        triggers {
            scm('* * * * *') // poll every minute
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
