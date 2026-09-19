pipeline {
  agent any

  tools {
    jdk 'jdk-17'
    maven 'maven'
    nodejs 'node'
  }

  environment {
    SPRING_DATASOURCE_URL      = 'jdbc:mysql://localhost:3306/smartcity?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true'
    SPRING_DATASOURCE_USERNAME = 'root'
    SPRING_DATASOURCE_PASSWORD = 'root'
    DOCKER_HUB_REGISTRY        = 'fatmathaouri'
  }

  stages {
    stage ("backend tests") {
      steps {
        sh '''
          docker run -d --name smartcity-mysql-test \
            -e MYSQL_ROOT_PASSWORD=root \
            -e MYSQL_DATABASE=smartcity \
            -p 3306:3306 mysql:8.0
        '''
        sh "cd smartcity && mvn test --batch-mode"
      }
      post {
        always {
          sh 'docker rm -f smartcity-mysql-test || true'
        }
      }
    }

    stage ("backend build") {
      steps {
        sh "cd smartcity && mvn package -DskipTests --batch-mode"
      }
    }

    stage ("frontend tests") {
      steps {
        sh "cd frontend && npm ci"
        sh "cd frontend && npm test -- --watch=false --browsers=ChromeHeadless"
      }
    }

    stage ("frontend build") {
      steps {
        sh "cd frontend && npm run build -- --configuration production"
      }
    }

    stage ("docker build and push") {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'docker-hub-creds',
          usernameVariable: 'DOCKER_HUB_USERNAME',
          passwordVariable: 'DOCKER_HUB_PASSWORD'
        )]) {
          sh '''
            echo $DOCKER_HUB_PASSWORD | docker login -u $DOCKER_HUB_USERNAME --password-stdin
            docker compose config
            docker compose build
            docker compose push backend frontend
            docker logout
          '''
        }
      }
    }

    stage ("docker deploy") {
      steps {
        sh '''
          docker compose pull
          docker compose down || true
          docker compose up -d
        '''
      }
    }
  }

  post {
    always {
      deleteDir()
    }
  }
}
