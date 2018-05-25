package com.mapbar.adas;

import com.mapbar.adas.BaseFragment.IPauseListener;
import com.mapbar.adas.BaseFragment.IResumeListener;
import com.mapbar.adas.BaseFragment.IStartListener;
import com.mapbar.adas.BaseFragment.IStopListener;

/**
 */
public interface ILifeCycleListener extends IStartListener, IResumeListener, IPauseListener, IStopListener, BaseFragment.IDestroyListener, BaseFragment.IConfigurationListener, BaseFragment.ICreateView {
}
