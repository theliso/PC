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

        private T providerResult;

        private bool done;

        private bool isBeingCalculated = false;

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
                                providerResult = provider();
                                isBeingCalculated = false;
                                done = true;
                                Monitor.PulseAll(monitor);
                            }
                            catch (InvalidOperationException)
                            {
                                Monitor.Pulse(monitor);
                                isBeingCalculated = true;
                                throw;
                            }
                        }
                    }
                } while (!done);

                return providerResult;
            }
        } // throws InvalidOperationException, ThreadInterruptedException
    }
}