pipeline {

  agent any

  stages {
    stage ("clean up"){
      steps {
        deleteDir()
      }
    }
    stage ("clone repo"){
      steps {
        sh "git clone https://github.com/fatmathaouri/smartcity.git"

      }

    }

  }

}
