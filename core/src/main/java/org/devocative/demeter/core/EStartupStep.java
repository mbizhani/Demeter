package org.devocative.demeter.core;

public enum EStartupStep {
	//NOTE: the order of following literals determines the startup steps of Demeter
	Begin,
	EncDec,
	Modules,
	Spring,
	PersistenceServices,
	Database,
	LazyBeans,
	BeansStartup,
	End;

	public static EStartupStep next(EStartupStep current) {
		int idx = current.ordinal();
		if (idx < (EStartupStep.values().length - 1)) {
			idx++;
		}
		return EStartupStep.values()[idx];
	}
}
