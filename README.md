# api-log
회사에서 주로 api 서버를 만드는데, api 서버 호출에 대한 audit log를 기록하고 싶어해서 일단은 간단한 api의 호출 로그를 기록한다. 

로그를 기록하는 방법은 다음과 같은 방법이 있다.
* 필터
* 인터셉터
* AOP

이중에서 인증을 고려한다면, 인터셉터나 AOP를 이용하는 것이 쉬울것 같다. 필터로 구현해야 한다면, 로그필터는 인증필터 뒤에 붙여서, 
인증과 인가가 수행된 후에 실제 API가 사용가능한 곳에서 로그를 수행 할 수 있도록 하여야 한다. 

샘플 프로그램은 다음의 환경에서 개발한다.

* IntelliJ 2024
* JDK 17
* DB - mysql
* Spring boot - 3.3.6
  *  spring data jpa
  *  springdoc

기본 rest sample은 다음의 sample를 참고하여 작성하였다. 원본에서는 
* https://www.callicoder.com/spring-boot-jpa-hibernate-postgresql-restful-crud-api-example/


## api-log-aop
AOP를 이용하는 방식이 가장 쉽게 접근할 수 있는 방법인데, API의 controller의 각 method의 호출을 로깅하는 경우에 
원본 Request와 Response가 로깅되는 게 아니라, method의 argument가 로깅되는 문제가 있다.

원본 Request와  Response를 로깅하려면, 필터나 인터셉터를 이용한 로깅을 수행해야 한다.

## api-log-interceptor
인터셉터를 이용한 구현은 기본적인 idea는 간단한데, 인터셉터에서의  request가 한번만 사용할 수 있어서? 사용이 직관적이지 않다.
특히 request와 response를  caching하기 위해서 ContentCachingFilter에서 사용한 ContentCachingRequestWrapper와 ContentCachingResponseWrapper는 
사용이 직관적이지 않아서 사용시에 주의를 기울여서 사용해야 한다.

특히 다음의 사항에 주의해야한다. 
* ContentCachingRequestWrapper는 doFilter를 수행한 후에야,  content가 설정되어 getContentAsByteArray() 같은 함수의 데이타를 정상적으로 이용할 수 있다.
* ContentCachingResponseWrapper는 Response 확인전에 cachingResponse.copyBodyToResponse()를 호출하여야 한다.

ContentCachingRequestWrapper와 인터셉터를 사용하여 로깅을 하는 방법은 다음의 방법을 참고하였다.
* https://velog.io/@dlwlrma/알고-쓰자-ContentCachingRequestWrapper

여기서 사용하는 기본 idea는 오류가 발생해도, 실행되는 afterCompletion의 성질을 이용하는 것이다.


