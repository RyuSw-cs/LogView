<img src = "https://github.com/RyuSw-cs/LogView/assets/72602912/3aa0b5cc-8cde-427b-9963-76cb50fc6a0f" width="60%" height="60%">

## 👩‍🏫PROJECT 소개
디버깅에 연결하지 않아도 로그를 볼 수 있도록 구현한 로그뷰입니다.

- **🙅‍♂️ 로그가 왜 필요할까요?**
    - 로그를 확인할려면 **개발자가 반드시 필요**해요.
      
    - 개발자는 오류가 나오지 않지만, **다른 국가나 특정 단말의 사용자는 오류가 발생**해요.
  
    - 개발 단계에서 **빠르게 오류를 해결**하고 싶어요.
 

- 🙆‍♂️ **로그뷰를 사용하면 어떻게 될까요?**
    - **기획자나 QA가 로그를 쉽게** 볼 수 있어요
  
    - 특정 단말에서 나오는 오류를 빠르게 해결할 수 있어요
  
    - 개발자가 **오류를 빠르게 확인**해서 수정할 수 있어요

🗓️ **작업기간** : 2023.12 ~ 2023.01

👨‍💻 **투입인원** : 안드로이드 개발(2명), 기획자(1명) - 안드로이드 개발 담당

📒 **담당업무**

- **라이브러리 구조 & 배포를 위한 Gradle 구조 설계**
    - **라이브러리 구조**
    
    ```kotlin
    └── 📂lib
        ├── 📂api
        ├── 📂callback
        ├── 📂config
        ├── 📂context
        ├── 📂exception
        ├── 📂service
        ├── 📂util
        ├── 📂view
        └── LogDataManager.java
    ```
    
    - **Gradle 셋팅 (배포 최적화)**
    
    ```groovy
    libraryVariants.configureEach { variant ->
            variant.outputs.configureEach {
                def df = new SimpleDateFormat("yyyyMMdd")
                df.setTimeZone(TimeZone.getDefault())
                outputFileName = "${project.name}_v${module_version}_${variant.buildType.name}_${df.format(new Date())}.aar"
            }
        }
    
    ext {
        module_version = "1.0.0.0"
    
        def df = new SimpleDateFormat("yyyy.MM.dd")
        df.setTimeZone(TimeZone.getDefault())
        releaseModule_path = "${rootProject.rootDir}/LogViewLib${df.format(new Date())}(v${module_version}-release)"
    }
    
    tasks.register('releaseModulePath', Delete) {
        delete releaseModule_path
    }
    
    tasks.register('makeModule') {
        dependsOn releaseModulePath
        doLast {
            println "************************************************************************************"
            println "***************************** LogView v$module_version release *****************************"
            println "************************************************************************************"
    
            copy {
                from "build/outputs/aar"
                into "$releaseModule_path/$project.name"
            }
        }
    }
    
    makeModule.mustRunAfter clean
    makeModule.dependsOn(['releaseModulePath', 'clean', 'assemble'])
    ```
    

- **View를 생성하기 위한 권한 요청 기능 구현**

  <img src = "https://github.com/RyuSw-cs/LogView/assets/72602912/d1e20bc7-1a59-4ec9-8e7d-42526098a0c1" width="30%" height="30%">



- **Service를 통해서 View 생성 구현**
- **adb 명령어를 통한 Log 컨트롤**

  <img src = "https://github.com/RyuSw-cs/LogView/assets/72602912/e9c552e3-77e4-4c23-996d-79b1a4761d67" width="30%" height="30%">  


    
- **그 외 유틸 기능을 API를 통해서 사용자가 설정하도록 구현**

  <img src = "https://github.com/RyuSw-cs/LogView/assets/72602912/3ae1b812-45b1-43f3-ab28-d65a816caf19" width="50%" height="50%">
    

## 🙇‍♂️ Experience
👍 **느낀점** 

- 서비스를 위한 앱이 아닌 서비스에 필요한 라이브러리를 개발해보니 gradle 버전이나 사용되는 라이브러리 버전 등, 다양한 사항이 고려되야 한다는 것을 배웠습니다.
- 사용자에게 라이브러리를 편리하게 제공하기 위해서 다양한 설정값과 API 제공방식이 필요했고 이를 설계하며 설계능력을 더 향상시킬 수 있었습니다.

🏆 **성과**

- 실제 사내 업무에 사용되어 배포된 앱에서 오류 발생 시, 기존에 대응하던 속도를 대폭감소
- 개발 문제가 아닌 사용자 단말, 나라 같은 특정 상황에서 빠르게 대응 가능
    - 실제 중국에서 Google이 안되는 현상을 로그뷰를 통해서 대응할 수 있었음

## 🙆🏻‍♂️Client(Android)

👨‍💻 **투입인원** : 2명

👨‍💻 **사용 OS** : Android

**📒 설계정보**

- Language: Java
- minSDK : 14
- gradle : 7.4

## 👩‍👩‍👦‍👦**Notion**
[https://various-event-01c.notion.site/a5f37fe4a101479f979a425c549adb0e?pvs=4](https://various-event-01c.notion.site/a5f37fe4a101479f979a425c549adb0e?pvs=4)
