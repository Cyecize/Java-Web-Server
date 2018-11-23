package com.cyecize.summer.areas.template.services;

import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.common.models.Model;

public interface TemplateRenderingService {
    String render(String view, Model model) throws ViewNotFoundException;
}
