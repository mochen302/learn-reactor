package com.ljm.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class OtherTest {
    public static void main(String[] args) throws Throwable {
        int a = 7;
        switch (a) {
            case 1: {
                final AtomicInteger countGenerate = new AtomicInteger(1);
                Flux.generate(sink -> {
                    sink.next(countGenerate.get() + " : " + new Date());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                    }
                    if (countGenerate.getAndIncrement() >= 5) {
                        sink.complete();
                    }
                }).subscribe(System.out::println);
                break;
            }
            case 2: {
                Flux.generate(() -> 1, (count, sink) -> {
                    sink.next(count + " : " + new Date());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (count >= 5) {
                        sink.complete();
                    }
                    return count + 1;
                }, System.out::println).subscribe(System.out::println);
                break;
            }
            case 3: {

                String key = "message";
                Mono<String> r = Mono.just("Hello")
                        .flatMap(s -> Mono.subscriberContext().map(ctx -> s + " " + ctx.get(key)))
                        .flatMap(s -> Mono.subscriberContext().map(ctx -> s + " " + ctx.get(key))).subscriberContext(ctx -> ctx.put(key, "Reactor"))
                        .subscriberContext(ctx -> ctx.put(key, "World"));
                StepVerifier.create(r).expectNext("Hello World Reactor")
                        .verifyComplete();
                break;
            }
            case 4: {
                AtomicInteger ai = new AtomicInteger();
                Function<Flux<String>, Flux<String>> filterAndMap = f -> {
                    final String value;
                    if (ai.incrementAndGet() != 1) {
                        value = "purple";
                    } else {
                        value = "orange";
                    }
                    return f.filter(color -> !color.equals(value)).map(String::toUpperCase);
                };

                Flux<String> compose = Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
//                        .compose(filterAndMap);//函数式可以是有状态的（stateful）
                        .transform(filterAndMap);//打包的函数式是无状态的
                compose.subscribe(d -> System.out.println("Subscriber 1 to Composed MapAndFilter :" + d));
                compose.subscribe(d -> System.out.println("Subscriber 2 to Composed MapAndFilter: " + d));

                break;
            }
            case 5: {

                StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofDays(1)))
                        .expectSubscription()
                        .expectNoEvent(Duration.ofDays(1))
                        .expectNext(0L)
                        .verifyComplete();
                break;
            }
            case 6: {
                TestPublisher<Integer> testPublisher = TestPublisher.<Integer>create().emit(1, 2, 3);
                StepVerifier.create(testPublisher.flux().map(i -> i * i)).expectNext(1, 4, 9).expectComplete();
                break;
            }
            case 7: {

                Flux<Integer> source = Flux.range(1, 3)
                        .doOnSubscribe(s -> System.out.println("上游收到订阅"));

                Flux<Integer> co = source.publish().refCount(2);

                co.subscribe(System.out::println, e -> {
                }, () -> {
                });
                co.subscribe(System.out::println, e -> {
                }, () -> {
                });

                System.out.println("订阅者完成订阅操作");
                Thread.sleep(500);
                System.out.println("还没有连接上");

//                co.connect();

                break;
            }

        }

    }

}
