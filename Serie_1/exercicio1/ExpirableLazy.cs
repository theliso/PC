using System;
using System.Collections.Generic;
using System.Threading;
using exercicio1.Properties;

namespace exercicio1
{
    public class ExpirableLazy<T> where T : class
    {
        private readonly object monitor = new object();

        private TimeSpan timeToLive;

        private TimeoutHolder timeOut;

        private Func<T> provider;

        private T providerResult; // provider result holder

        private bool done; // tells if the call to the provider as successfully returned or not

        private bool isBeingCalculated = false; // indicates if a thread called the provider or not

        public ExpirableLazy(Func<T> provider, TimeSpan timeToLive)
        {
            this.timeToLive = timeToLive;
            timeOut = new TimeoutHolder(timeToLive.Milliseconds);
            this.provider = provider;
            done = false;
        }

        public T value
        {
            get
            {
                lock (monitor)
                {
                    if (done && timeOut.Value > 0)
                    {
                        return providerResult;
                    }
                    do
                    {
                        if (!done || timeOut.Value == 0)
                        {
                            if (isBeingCalculated)
                            {
                                try
                                {
                                    Monitor.Wait(monitor);
                                }
                                catch (ThreadInterruptedException)
                                {
                                    throw;
                                }
                            }
                            else
                            {
                                try
                                {
                                    timeOut = new TimeoutHolder(timeToLive.Milliseconds);
                                    Monitor.Exit(monitor);
                                    providerResult = provider();
                                    Monitor.Enter(monitor);
                                    isBeingCalculated = false;
                                    done = true;
                                    Monitor.PulseAll(monitor);
                                }
                                catch (InvalidOperationException)
                                {
                                    Monitor.Enter(monitor);
                                    Monitor.Pulse(monitor);
                                    isBeingCalculated = true;
                                    throw;
                                }
                            }
                        }
                    } while (!done);
                }

                return providerResult;
            }
        } // throws InvalidOperationException, ThreadInterruptedException
    }
}