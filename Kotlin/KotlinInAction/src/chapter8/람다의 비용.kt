package chapter8

import sun.rmi.runtime.Log
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.locks.Lock

/** 인라인 함수를 통해 람다의 부가 비용 없애기
 * 람다식과 동일한 자바 문장 중 람다식이 더 느리진 않을까??
 * 실제 람다가 변수를 포획하면 새로운 무명 클래스 객체가 생기는데,
 * 이런 경우 실행 시점에 무명 클래스 생성에 따른 부가 비용이 든다.
 * 동일한 구현에 대해서는 일반 함수가 더 효율적이다.
 * inline 변경자를 사용하면 그 함수를 호출하는
 * 모든 문장을 함수 본문에 해당하는 바이트코드로 바꿔치기 해준다.*/


/** 어떤 함수를 inline으로 선언하면 그 함수의 본문이 인라인되고
 * 호출하는 코들르 함수를 호출하는 바이트코드 대신에 함수 본문을 번역한 바이트 코드로 컴파일 된다!! (이게 중요)
 * 밑의 잠금 예제를 살펴 보자
 */

/** 365p 인라인 함수 정의 */
inline fun<T> synchronized(lock: Lock, action: () -> T): T {
    lock.lock()
    try {
        return action()
    } catch (e : Exception) {
        e.printStackTrace()
    } finally {
        lock.unlock()
    }
    return throw Exception("인자로 넘겨준 action이 정상 작동하지 않았습니다.")
}

/*
해당 synchronized메소드를 호출 할 때 람다식을 파라미터로 받아서 다른 객체에 저장하고, 이를 호출하게 되면
람다의 코드를 알 수 없기에 인라이닝 되지 않는다. 즉, 함수가 파라미터로 받은 람다를 호출한다면
그 호출을 쉽게 람다 본문으로 바꿀 수 있지만, 파라미터로 받은 람다를 다른 변수에 저장하고
나중에 그 변수를 사용한다면 람다를 표현하는 객체가 어딘가는 존재해야 하기 때문에 람다를
인라이닝 할 수 없다.
 */


/** 다음은 모든 시퀀스 원소에 파라미터로 받은 람다를 적용한 새 시컨스를 반환하는 함수 예제 */
//fun <T, R> Sequence<T>.map(transform: (T) -> (R)): Sequence<R> {
//    return TransformingSequence(this, transform)
//}

/* 시퀀스 메서드는 파라미터로 받은 함수 값을 그대로 TransFormingSequence 생성자에 인자로 넘긴다.
생성자는 전달 받은 람다를 프로퍼티로 저장하고, 이러면 인라이닝 되지 않으므로
일반적인 함수 표현으로 만들 수 밖에 없다. 이러면 transform을 함수 인터페이스를 구현하는
무명 클래스 인스턴스로 만들어야만 한다.
만약 둘 이상의 람다를 인자로 받는 함수에서 일부 람다만 인라이닝하고 싶은 때는 noinline 변경자를
파라미터 이름 앞에 붙여서 인라이닝을 금지할 수 있다. (람다에 너무 많은 코드가 들어가면 인라이닝 하면 0안되므로...)
 */
inline fun foo (inlined: () -> Unit, noinline notInlined: () -> Unit) {

}

/* 시퀀스의 인라이닝에 관한 간단한 고찰 371P */
/** 컬렉션을 filter와 map을 연쇄해서 사용하게 되면 두 함수 모두 inline함수이기 때문에 함수 본문은 인라이닝되며,
 * 추각 객체나 클래스 생성은 없다. 다만, 중간 리스트를 만들기 때문에 처리할 원소가 많아지면 부가 비용이 커진다.
 * 이런 경우 asSequence()를 통해 리스트 대신 시퀀스를 사용하면 중간 리스트로 인한 부가 비용은 줄어든다.
 * 그런데 또 문제가 있다. 이때 각 중간 시퀀스는 람다를 필드에 저장하는 객체로 표현되며,
 * 최종 연산은 중간 시퀀스에 있는 여러 람다를 연쇄 호출한다. 따라서 시퀀스는(람다를 저장해야 하므로) 람다를 인라인하지 않는다.
 * 따라서 지연 계산을 통해 성능을 향상시키려는 이유로 모든 컬렉션 연산에 asSequence를 붙여서는 안 된다.
 * 시퀀스 연산에서는 람다가 인라이닝되지 않기 때문에 크기가 작은 컬렉션은 오히려 일반 컬렉션 연산이 더 성능이 나올 수도 있다.
 * 시퀀스를 통해 성능을 향상시킬 수 있는 경우는 컬렉션 크기가 큰 경우뿐이다. <- 이게 가장 중요
 */


/** 함수를 인라인으로 선언해야 하는 경우
 * inline 키워드의 이점을 배우고 나면 코드를 더 빠르게 만들기 위해 코드 여기저기에서 inline을 사용하고 싶어질 것이다.
 * 하지만 inline 키워드를 사용해도 람다를 인자로 받는 함수만 성능이 좋아질 가능성이 높다.
 * 일반 함수 호출의 경우 JVM은 이미 강력하게 인라이닝을 지원한다. JVM은 코드 실행을 분석해서 가장 이익이 되는 방향으로
 * 호출을 인라이닝한다. 이런 과정은 바이트코드를 실제 기계어 코드로 번역하는 과정(JIT)에서 일어난다. 이런 JVM의 최적화를
 * 활용한다면 바이트코드에서는 각 함수 구현이 정확히 한 번만 있으면 되고, 그 함수를 호출하는 부분에서 따로 함수 코드를
 * 중복할 필요가 없다. 반면, 코틀린 인라인 함수는 바이트코드에서 각 함수 호출 지점을 함수 본문으로 대치하기 때문에
 * 코드 중복이 생긴다. 게다가 함수를 직접 호출하면 스택 트레이스가 더 깔끔해진다.
 *
 * 반면, 람다를 인자로 받는 함수를 인라이닝하면 이익이 더 많다.
 * 첫째, 인라이닝을 통해 없앨 수 있는 부가 비용이 상당하다. 함수 호출 비용을 줄일 수 있을 뿐 아니라
 * 람다를 표현하는 클래스와 람다 인스턴스에 해당하는 객체를 만들 필요도 없어진다.
 * 둘째, 현재의 JVM은 함수 호출과 람다를 인라이닝해 줄 정도로 똑똑하지는 못하다.
 * 마지막으로 인라이닝을 사용하면 일반 람다에서는 사용할 수 없는 몇 가지 기능을 사용 할 수 있다. 그런 기능 중에는
 * 넌로컬(non-local)반환이 있다.
 *
 * 하지만 inline 변경자를 함수에 붙일 때는 코드 크기에 주의를 기울여야 한다. 인라이닝하는 함수가 큰 경우
 * 함수의 본문에 해당하는 바이트코드를 모든 호출 지점에 복사해 넣고 나면 바이트코드가 전체적으로 아주 커질 수 있다.
 * 그런 경우 람다 인자와 무관한 코드를 별도의 비인라인 함수로 빼낼 수도 있다. 실제로 코틀린 표준 라이브러리에서 제공하는
 * inline함수들을 보면 모두 크기가 아주 작다는 사실을 알 수 있다.
 */


/** 람다로 중복을 없앨 수 있는 일반적인 패턴 중 한 가지는 어떤 작업을 하기 전에 자원을 획득하고 작업을 마친 후
 * 자원을 해제하는 자원 관리다. 여기서 자원(Resource)은 파일, 락, DB 트랜잭션 등 여러 다른 대상을 가리킬 수 있다.
 * 자원 관리 패턴을 만들 때 보통 사용하는 방법은 try/finally문을 사용하되 try블록을 시작하기 직전에
 * 자원을 확득하고 finally 블록에서 자원을 해제하는 것이다.
 */

/** 자바에는 synchronized(..)함수는 락 객체를 인자로 취하는 함수로 자바 7부터는 try-with-resource문이 생겼다 */
/** 코틀린 표준라이브러리에도 유사한 함수가 있는데 바로 use함수다. */

fun readFirstLineFromFile (path: String): String {
    BufferedReader(FileReader(path)).use { br ->
        return br.readLine()
    }
}
/* use함수는 닫을 수 있는 closeable자원에 대한 확장 함수며, 람다를 인자로 받는다. use는 람다를 호출한 다음에 자원을 닫아준다.
이때 람다가 정상 종료한 경우는 물론 람다 안에서 예외가 발생한 경우에도 자원을 확실히 닫는다. 물론 use함수는 인라인함수다.
람다의 본문 안에서 사용한 return은 넌로컬 return이다. 이 return문은 람다가 아니라 readFirstLineFromFile함수를 끝내면서 값을 반환한다.
 */


data class Personn(val name: String, val age: Int)

val people = listOf(Personn("Alice", 29), Personn("Bob", 31))

fun lookForAlice(people: List<Personn>) {
    for(person in people) {
        if (person.name == "Alice") {
            println("Found!")
            return
        }
    }
    println("Alice is not found")
}

fun lookForAlice2(people: List<Personn>) {
    people.forEach { person ->
        if (person.name == "Alice") {
            println("Found!")
            return // 넌 로컬(non-local) return문
        }
    }
    println("Alice is not found")
}

/** 여기서 람다를 인자로 받는 함수는 오직 인라인 함수인 경우뿐이다.
 * forEach는 인라인 함수이므로 람다 본문과 함께 인라이닝된다. 따라서 return식이 바깥쪽 함수를
 * 반환시키도록 쉽게 컴파일할 수 있다. 인라이닝되지 않는 함수는 람다를 변수에 저장할 수 있고,
 * 바깥쪽 함수로부터 반환된 뒤에 저장해 둔 람다가 호출될 수도 있다. 그런 경우
 * 람다 안의 return이 실행되는 시점이 바깥쪽 함수를 반환시키기엔 너무 늦은 시점일 수도 있다.
 */

/** 무명함수는 람다 식을 대신할 수 있으며 return 식을 처리하는 규칙이 일반 람디 식과는 다름다.
 * 여러곳에서 return해야 하는 코드 블록을 만들어야 한다면 람다 대신 무명 함수를 쓸 수 있다.
 * (무명 함수는 local return함.)
 */

fun main() {
    lookForAlice2(listOf(Personn("Alice", 123)))
}




