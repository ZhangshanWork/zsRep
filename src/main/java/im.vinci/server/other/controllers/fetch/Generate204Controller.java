package im.vinci.server.other.controllers.fetch;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 为wifi验证用的,只返回204
 * Created by tim@vinci on 16/7/9.
 */
@Controller
public class Generate204Controller {
    @RequestMapping("generate_204")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public String generate_204() {
        return "";
    }
}
