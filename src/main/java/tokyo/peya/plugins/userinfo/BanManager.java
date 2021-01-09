package tokyo.peya.plugins.userinfo;

import ml.peya.api.*;
import ml.peya.plugins.*;

import java.util.*;

public class BanManager
{
    BanManagerAPI api;
    public BanManager()
    {
        api = PeyangGreatBanManager.getAPI();
        Userinfo.getThisOne().banEnabled = true;
    }

    public long getBans(UUID p)
    {
        return api.getBans(p).size();
    }
}
