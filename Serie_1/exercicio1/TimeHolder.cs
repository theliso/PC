using System;
using System.Threading;

namespace exercicio1.Properties
{
    public struct TimeoutHolder {
        private int timeout;
        private int refTime;
	
        public TimeoutHolder(int timeout) {
            this.timeout = timeout;
            this.refTime = (timeout != 0 && timeout != Timeout.Infinite) ? Environment.TickCount : 0;
        }
	
        // returns the remaining timeout
        public int Value {
            get {
                if (timeout != 0 && timeout != Timeout.Infinite) {
                    int now = Environment.TickCount;
                    if (now != refTime) {
                        int elapsed = now - refTime;
                        refTime = now;
                        timeout = elapsed < timeout ? timeout - elapsed : 0;
                    }
                }
                return timeout;
            }
        }
    }
}