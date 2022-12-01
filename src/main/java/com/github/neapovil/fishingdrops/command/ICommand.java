package com.github.neapovil.fishingdrops.command;

public interface ICommand
{
    void register();

    default String commandName()
    {
        return "fishingdrops";
    }

    default String permission()
    {
        return "fishingdrops.command.admin";
    }
}
