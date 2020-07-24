package com.cyecize.toyote.services;

import java.io.File;
import java.io.IOException;

public interface Tika {

    String detect(File file) throws IOException;
}
