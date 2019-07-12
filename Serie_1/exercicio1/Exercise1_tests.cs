using System;
using System.Threading;

namespace exercicio1
{
    public class Exercise1_tests
    {
        private const int NUMBER_OF_THREADS = 100;
        public static void Main(string[] args)
        {
            ExpirableLazy<string> expirableLazy = new ExpirableLazy<String>(() => "Hello World", new TimeSpan(1000));
            Thread[] threads = new Thread[NUMBER_OF_THREADS];
            for (int i = 0; i < NUMBER_OF_THREADS; ++i)
            {
                threads[i] = new Thread(() =>
                {
                    Console.WriteLine("result: " + expirableLazy.value);
                });
                threads[i].Start();
            }
            Console.Read();
        }
    }
}