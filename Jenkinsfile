pipeline {
  agent any
  stages {
    stage('GIT Configuration') {
      steps {
        sh '''# Git set user name, email, default editor and credentials policy
echo "Setting Git configuration..."
git config --global user.name "$GIT_ADMIN_FIRST_NAME $GIT_ADMIN_LAST_NAME"
git config --global user.email "$GIT_ADMIN_EMAIL"
git config --global core.editor vim
git config --global credential.helper store
'''
      }
    }

  }
}