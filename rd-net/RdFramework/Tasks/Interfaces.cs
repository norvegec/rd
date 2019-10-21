using System;
using JetBrains.Annotations;
using JetBrains.Collections.Viewable;
using JetBrains.Lifetimes;

namespace JetBrains.Rd.Tasks
{
  [PublicAPI] 
  public interface IRdTask<T>
  {
    IReadonlyProperty<RdTaskResult<T>> Result { get; }
  }
  
  public interface IRdEndpoint<TReq, TRes>
  {
    void Set(Func<Lifetime, TReq, RdTask<TRes>> handler);
  }

  public interface IRdCall<in TReq, TRes>
  {
    TRes Sync(TReq request, RpcTimeouts timeouts = null);
    IRdTask<TRes> Start(TReq request, IScheduler responseScheduler = null);

    IRdTask<TRes> Start(Lifetime lifetime, TReq request, IScheduler responseScheduler = null);
  }
}