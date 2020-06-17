package fm.douban.app.control;

import fm.douban.model.Singer;
import fm.douban.service.SingerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/8 21:17
 */
@Controller
public class SingerControl {
    @Autowired
    private SingerService singerService;
    @GetMapping("/user-guide")
    public String myMhz(Model model){
        List<Singer> singerList = randomSingers();
        if (singerList!=null){
            model.addAttribute("singers",singerList);
        }else {
            System.out.println("无发查询到数据！");
            return "";
        }
        return "userguide";
    }

    @GetMapping("/singer/random")
    @ResponseBody
    public List<Singer> randomSingers(){
        List<Singer> singerList = singerService.getAll();
        List<Singer> randomlist = new ArrayList<>();
        for (int i=0;i<10;i++){
            Random random = new Random();
            int num = random.nextInt(singerList.size());
            randomlist.add(singerList.get(num));
        }
        return randomlist;
    }
}
