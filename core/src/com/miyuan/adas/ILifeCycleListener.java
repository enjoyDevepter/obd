package com.miyuan.adas;

import com.miyuan.adas.BaseFragment.IPauseListener;
import com.miyuan.adas.BaseFragment.IResumeListener;
import com.miyuan.adas.BaseFragment.IStartListener;
import com.miyuan.adas.BaseFragment.IStopListener;

/**
 */
public interface ILifeCycleListener extends IStartListener, IResumeListener, IPauseListener, IStopListener, BaseFragment.IDestroyListener, BaseFragment.IConfigurationListener, BaseFragment.ICreateView {
}
